import request, { download } from '@/utils/request'

// 院落列表
export function listCourtyard(query) { return request({ url: '/inspect/courtyard/all', method: 'get', params: query }) }

// 日志列表
export function listLog(query) { return request({ url: '/inspect/log/list', method: 'get', params: query }) }

// 日志详情
export function getLog(logId) { return request({ url: '/inspect/log/' + logId, method: 'get' }) }

// 管理员订正
export function updateLog(data) { return request({ url: '/inspect/log', method: 'put', data: data }) }

// 统计数据
export function getStatistics(query) { return request({ url: '/inspect/log/statistics', method: 'get', params: query }) }

// 特殊人员档案（支持按巡查小类筛选：tenant租户 solo独居）
export function listTenant(query) { return request({ url: '/inspect/log/tenantList', method: 'get', params: query }) }

// 导出
export function exportLog(query) { return download('/inspect/log/export', query, `巡查日志_${new Date().getTime()}.xlsx`) }
