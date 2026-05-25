import request from './index'

export interface SuggestionRequest {
  tags: string
  location: string
  query: string
}

export interface SuggestionResponse {
  suggestion: string
}

export interface SummaryRequest {
  title: string
  participants: string
  reviews: string
}

export interface SummaryResponse {
  summary: string
}

/**
 * 获取活动创意建议
 */
export const getSuggestion = (data: SuggestionRequest) => {
  return request.post<SuggestionResponse>('/ai/suggest', data)
}

/**
 * 获取活动结束后的 AI 社交总结
 */
export const getActivitySummary = (data: SummaryRequest) => {
  return request.post<SummaryResponse>('/ai/activity-summary', data)
}
