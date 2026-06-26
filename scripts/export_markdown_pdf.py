import argparse
import html
import re
import subprocess
import sys
import tempfile
from pathlib import Path
from urllib.parse import quote

import markdown


EDGE_CANDIDATES = [
    Path(r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"),
    Path(r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"),
    Path(r"C:\Program Files\Google\Chrome\Application\chrome.exe"),
    Path(r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"),
]


def find_browser() -> Path:
    for candidate in EDGE_CANDIDATES:
        if candidate.exists():
            return candidate
    raise FileNotFoundError("未找到可用的 Edge/Chrome 浏览器。")


def path_to_file_uri(path: Path) -> str:
    safe_chars = ":/()!~*'"
    return f"file:///{quote(path.resolve().as_posix(), safe=safe_chars)}"


def rewrite_local_images(markdown_text: str, base_dir: Path) -> str:
    pattern = re.compile(r"!\[([^\]]*)\]\(([^)]+)\)")

    def replace(match: re.Match[str]) -> str:
        alt_text = match.group(1)
        raw_target = match.group(2).strip()
        target = raw_target.strip("<>")

        if target.startswith(("http://", "https://", "file:///")):
            return match.group(0)

        if re.match(r"^[A-Za-z]:[\\/]", target):
            image_path = Path(target)
        else:
            image_path = (base_dir / target).resolve()

        if not image_path.exists():
            return match.group(0)

        return f"![{alt_text}]({path_to_file_uri(image_path)})"

    return pattern.sub(replace, markdown_text)


def build_html(title: str, body_html: str) -> str:
    css = r"""
    @page {
      size: A4;
      margin: 18mm 15mm 16mm 15mm;
    }

    :root {
      --text: #222;
      --muted: #666;
      --line: #cfcfcf;
      --header-bg: #f5f5f5;
    }

    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      color: var(--text);
      font-family: "Microsoft YaHei", "PingFang SC", "Noto Sans CJK SC", sans-serif;
      font-size: 13px;
      line-height: 1.7;
      background: #fff;
    }

    .doc {
      width: 100%;
    }

    h1, h2, h3, h4, h5, h6 {
      page-break-after: avoid;
      break-after: avoid;
      line-height: 1.35;
      margin-top: 1.1em;
      margin-bottom: 0.45em;
    }

    h1 { font-size: 24px; }
    h2 { font-size: 21px; }
    h3 { font-size: 18px; }
    h4 { font-size: 16px; }

    p, div, ul, ol, blockquote, pre {
      margin-top: 0.45em;
      margin-bottom: 0.45em;
    }

    ul, ol {
      padding-left: 1.4em;
    }

    code {
      font-family: Consolas, "Courier New", monospace;
      font-size: 0.95em;
      background: #f7f7f7;
      padding: 0.08em 0.28em;
      border-radius: 3px;
    }

    pre {
      padding: 10px 12px;
      border: 1px solid #e7e7e7;
      background: #fafafa;
      overflow: hidden;
      white-space: pre-wrap;
      word-break: break-word;
    }

    pre code {
      background: transparent;
      padding: 0;
    }

    table {
      width: 100% !important;
      border-collapse: collapse !important;
      table-layout: fixed;
      margin: 6px 0 12px 0 !important;
      page-break-inside: auto;
      break-inside: auto;
    }

    h3 + table,
    h4 + table,
    h3 + div + table,
    h4 + div + table,
    h3 + p + table,
    h4 + p + table {
      margin-top: 6px !important;
    }

    th, td {
      border: 1px solid var(--line);
      padding: 6px 8px;
      vertical-align: top;
      text-align: left;
      word-break: break-word;
      overflow-wrap: anywhere;
    }

    thead th {
      background: var(--header-bg);
    }

    tr {
      page-break-inside: avoid;
      break-inside: avoid;
    }

    img {
      max-width: 100%;
      height: auto;
    }

    hr {
      border: none;
      border-top: 1px solid #e0e0e0;
      margin: 1em 0;
    }
    """

    return f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>{html.escape(title)}</title>
  <style>{css}</style>
</head>
<body>
  <main class="doc">
    {body_html}
  </main>
</body>
</html>
"""


def render_markdown(markdown_path: Path) -> str:
    text = markdown_path.read_text(encoding="utf-8")
    text = rewrite_local_images(text, markdown_path.parent)
    return markdown.markdown(
        text,
        extensions=["tables", "fenced_code", "sane_lists", "nl2br"],
        output_format="html5",
    )


def export_pdf(markdown_path: Path, output_pdf: Path) -> Path:
    browser = find_browser()
    body_html = render_markdown(markdown_path)
    page_html = build_html(markdown_path.stem, body_html)

    with tempfile.TemporaryDirectory() as temp_dir:
        temp_path = Path(temp_dir)
        html_path = temp_path / f"{markdown_path.stem}.html"
        html_path.write_text(page_html, encoding="utf-8")

        cmd = [
            str(browser),
            "--headless=new",
            "--disable-gpu",
            "--disable-software-rasterizer",
            "--disable-extensions",
            "--disable-dev-shm-usage",
            "--no-first-run",
            "--no-default-browser-check",
            "--no-sandbox",
            "--allow-file-access-from-files",
            "--enable-local-file-accesses",
            "--print-to-pdf-no-header",
            f"--print-to-pdf={output_pdf.resolve()}",
            f"--user-data-dir={temp_path / 'browser-profile'}",
            path_to_file_uri(html_path),
        ]

        completed = subprocess.run(cmd, capture_output=True)
        if completed.returncode != 0:
            stdout = completed.stdout.decode("utf-8", errors="replace")
            stderr = completed.stderr.decode("utf-8", errors="replace")
            raise RuntimeError(
                "PDF 导出失败。\n"
                f"stdout:\n{stdout}\n"
                f"stderr:\n{stderr}"
            )

    return output_pdf


def main() -> int:
    parser = argparse.ArgumentParser(description="使用 Edge/Chrome 将 Markdown 导出为 PDF。")
    parser.add_argument("input", help="输入的 Markdown 文件路径")
    parser.add_argument(
        "-o",
        "--output",
        help="输出 PDF 路径；默认与 Markdown 同名并追加 .edge.pdf",
    )
    args = parser.parse_args()

    markdown_path = Path(args.input).resolve()
    if not markdown_path.exists():
        print(f"输入文件不存在: {markdown_path}", file=sys.stderr)
        return 1

    if args.output:
        output_pdf = Path(args.output).resolve()
    else:
        output_pdf = markdown_path.with_name(f"{markdown_path.stem}.edge.pdf")

    try:
        export_pdf(markdown_path, output_pdf)
    except Exception as exc:  # noqa: BLE001
        print(str(exc), file=sys.stderr)
        return 1

    print(f"PDF 已导出到: {output_pdf}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
