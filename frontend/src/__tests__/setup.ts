import { vi } from 'vitest'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  },
  ElForm: { name: 'ElForm' },
  ElFormItem: { name: 'ElFormItem' },
  ElInput: { name: 'ElInput' },
  ElButton: { name: 'ElButton' },
}))

// Mock vue-router - 创建可被正确 mock 的对象
const mockUseRouter = vi.fn()
const mockUseRoute = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: mockUseRouter,
  useRoute: mockUseRoute,
  RouterLink: { name: 'RouterLink' },
}))

// Mock Pinia stores
vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    loginAction: vi.fn(),
    registerAction: vi.fn(),
    logout: vi.fn(),
  }),
}))

// Mock API
vi.mock('@/api/index', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock as any

// Mock URL.createObjectURL 和 URL.revokeObjectURL
if (!global.URL) {
  global.URL = {} as any
}
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url')
global.URL.revokeObjectURL = vi.fn()
