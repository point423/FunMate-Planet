import request from './index'

export interface SuggestionRequest {
  tags: string
  location: string
  query: string
}

export interface SuggestionResponse {
  suggestion: string
}

/**
 * 获取活动创意建议
 */
export const getSuggestion = (data: SuggestionRequest) => {
  return request.post<SuggestionResponse>('/ai/suggest', data)
}
