import request from '@/utils/request'

// 查询图书列表
export function listBook(query) {
    return request({
        url: '/library/book/list',
        method: 'get',
        params: query
    })
}

export function addBook(data) {
    return request({
        url: '/library/book/add',
        method: 'post',
        data: data
    })
}