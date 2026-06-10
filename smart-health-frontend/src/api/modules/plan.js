import request from '../request'

export function generatePlan() {
  return request.post('/plan/generate', undefined, { timeout: 60000 })
}

export function getPlanHistory() {
  return request.get('/plan/history')
}

export function getPlanDetail(id) {
  return request.get(`/plan/${id}`)
}

export function getGenerateCount() {
  return request.get('/plan/generate-count')
}

export function normalizeGenerateCount(count) {
  if (!count) {
    return undefined
  }
  return {
    used: count.used ?? count.usedCount ?? 0,
    limit: count.limit ?? count.limitCount ?? 0,
    remaining: count.remaining ?? count.remainingCount ?? 0
  }
}

export function normalizePlanDetail(plan) {
  if (!plan) {
    return plan
  }

  return {
    ...plan,
    planContent: normalizePlanContent(plan.planContent)
  }
}

export function formatPlanLevel(level) {
  const labels = {
    BASIC: '基础计划',
    VIP: 'VIP 计划',
    基础版: '基础计划'
  }
  return labels[level] || level || '-'
}

export function formatPlanType(type) {
  const labels = {
    DIET: '饮食计划',
    EXERCISE: '运动计划',
    COMBINED: '综合计划'
  }
  return labels[type] || type || '-'
}

function normalizePlanContent(content) {
  if (!content) {
    return {}
  }

  if (typeof content === 'string') {
    return parsePlanJson(content) || {}
  }

  if (content.raw && !content.dietPlan && !content.exercisePlan) {
    return parsePlanJson(content.raw) || content
  }

  return content
}

function parsePlanJson(value) {
  if (typeof value !== 'string') {
    return undefined
  }

  const text = value.trim()
  const candidates = [
    text,
    text.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, ''),
    text.match(/\{[\s\S]*\}/)?.[0]
  ].filter(Boolean)

  for (const candidate of candidates) {
    try {
      return JSON.parse(candidate)
    } catch {
      // Try the next possible JSON shape.
    }
  }

  return undefined
}
