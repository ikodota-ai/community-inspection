import request from '@/utils/request'

export function listAddress(query) { return request({ url: '/inspect/address/list', method: 'get', params: query }) }
export function getAddress(id) { return request({ url: '/inspect/address/' + id, method: 'get' }) }
export function addAddress(data) { return request({ url: '/inspect/address', method: 'post', data: data }) }
export function updateAddress(data) { return request({ url: '/inspect/address', method: 'put', data: data }) }
export function delAddress(id) { return request({ url: '/inspect/address/' + id, method: 'delete' }) }
