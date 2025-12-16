package pe.edu.idat.dsi.dami.idatgram.data.remote.dto

import pe.edu.idat.dsi.dami.idatgram.data.entity.Comment

data class CommentDto(
    val id: String,
    val postId: String,
    val userId: String,
    val text: String,
    val likesCount: Int? = 0,
    val repliesCount: Int? = 0,
    val parentCommentId: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

fun CommentDto.toEntity() = Comment(
    id = id,
    postId = postId,
    userId = userId,
    text = text,
    likesCount = likesCount ?: 0,
    repliesCount = repliesCount ?: 0,
    parentCommentId = parentCommentId,
    createdAt = createdAt ?: System.currentTimeMillis(),
    updatedAt = updatedAt ?: System.currentTimeMillis()
)
