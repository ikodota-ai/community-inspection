import request from '@/utils/request'

// 人员列表
export function listUser(query) { return request({ url: '/inspect/user/list', method: 'get', params: query }) }

// 设置用户院落
export function setCourtyard(data) { return request({ url: '/inspect/user/courtyard', method: 'put', data: data }) }
