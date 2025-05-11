package com.fakebook.SocialMediaApp.helpers

import com.fakebook.SocialMediaApp.models.Post
import com.fakebook.SocialMediaApp.models.User

object RankingUtils {

    // 1. Rank posts by engagement
    fun rankPosts(posts: List<Post>): List<Post> {
        return posts.map { post ->
            post.score = calculatePostScore(post)
            post
        }.sortedByDescending { it.score }
    }

    // 2. Rank posts based on search query
    fun rankPostSearch(searchQuery: String, posts: List<Post>): List<Post> {
        val query = searchQuery.trim().lowercase()
        return posts.map { post ->
            val relevanceScore = calculatePostSearchScore(query, post)
            post.score = relevanceScore
            post
        }.sortedByDescending { it.score }
    }

    // 3. Rank users based on search query
    fun rankUserSearch(searchQuery: String, users: List<User>): List<User> {
        val query = searchQuery.trim().lowercase()
        return users.map { user ->
            val relevanceScore = calculateUserSearchScore(query, user)
            user.score = relevanceScore
            user
        }.sortedByDescending { it.score }
    }

    // region Scoring Functions

    private fun calculatePostScore(post: Post): Double {
        // Weighted: likes (1.0), comments (1.5), tags (0.5)
        return post.likes * 1.0 + post.comments * 1.5 + post.tags.size * 0.5
    }

    private fun calculatePostSearchScore(query: String, post: Post): Double {
        var score = 0.0
        if (post.caption.lowercase().contains(query)) score += 3.0
        if (post.username.lowercase().contains(query)) score += 2.0
        score += post.tags.count { it.lowercase().contains(query) } * 1.5
        return score
    }

    private fun calculateUserSearchScore(query: String, user: User): Double {
        var score = 0.0
        if (user.username.lowercase().contains(query)) score += 3.0
        if (user.fullName.lowercase().contains(query)) score += 2.0
        if (user.email.lowercase().contains(query)) score += 1.5
        if (user.bio.lowercase().contains(query)) score += 1.0
        return score
    }

    // endregion
}
