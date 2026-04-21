package com.mindnote.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.ktor.client.plugins.ResponseException
import java.io.IOException

class ChatPagingSource(
    private val api: ChatApi,
    private val conversationId: String,
) : PagingSource<Long, ChatMessageDto>() {

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ChatMessageDto> {
        val before = params.key
        return try {
            val page = api.messages(
                conversationId = conversationId,
                before = before,
                limit = params.loadSize,
            )
            val nextKey = if (page.size < params.loadSize) null else page.last().createdAt
            LoadResult.Page(data = page, prevKey = null, nextKey = nextKey)
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: ResponseException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Long, ChatMessageDto>): Long? = null
}
