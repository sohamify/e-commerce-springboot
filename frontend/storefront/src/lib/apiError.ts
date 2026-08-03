import { isAxiosError } from 'axios'
import type { ApiProblem } from '../types/auth'

export function apiErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (isAxiosError<ApiProblem>(error) && error.response?.data?.detail) {
    return error.response.data.detail
  }
  return fallback
}

export function apiErrorCode(error: unknown): string | undefined {
  if (isAxiosError<ApiProblem>(error)) {
    return error.response?.data?.errorCode
  }
  return undefined
}

/** Per-field validation messages from a 400 VALIDATION_FAILED response, keyed by field name. */
export function apiFieldErrors(error: unknown): Record<string, string> {
  if (isAxiosError<ApiProblem>(error)) {
    return error.response?.data?.fieldErrors ?? {}
  }
  return {}
}
