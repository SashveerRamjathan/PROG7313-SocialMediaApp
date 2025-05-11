package com.fakebook.SocialMediaApp.helpers

import com.fakebook.SocialMediaApp.models.Post
import com.fakebook.SocialMediaApp.models.User

/**
 * `RankingUtils` provides utility methods to rank posts and users
 * based on engagement metrics or relevance to search queries.
 *
 * Responsibilities include:
 * - Scoring and ranking posts by user interaction (likes, comments, tags)
 * - Scoring and ranking posts and users based on search query relevance
 *
 * This class is stateless and all functions are static.
 */
object RankingUtils {

    /**
     * Ranks a list of posts based on engagement score, which is calculated
     * using a weighted formula considering likes, comments, and tags.
     *
     * @param posts List of [Post] objects to be ranked.
     * @return A list of posts sorted in descending order of engagement.
     */
    fun rankPosts(posts: List<Post>): List<Post> {
        return posts.map { post ->
            // Calculate and assign a score to each post
            post.score = calculatePostScore(post)
            post
        }.sortedByDescending { it.score }
    }

    /**
     * Ranks a list of posts based on how well they match a given search query.
     * Relevance is determined from matches in caption, username, and tags.
     *
     * @param searchQuery The user's search input.
     * @param posts List of [Post] objects to be ranked.
     * @return A list of posts sorted by relevance to the search query.
     */
    fun rankPostSearch(searchQuery: String, posts: List<Post>): List<Post> {
        val query = searchQuery.trim().lowercase()
        return posts.map { post ->
            // Calculate how relevant each post is to the query
            val relevanceScore = calculatePostSearchScore(query, post)
            post.score = relevanceScore
            post
        }.sortedByDescending { it.score }
    }

    /**
     * Ranks a list of users based on how well their profile attributes match a search query.
     * Matches in username, full name, email, and bio affect the relevance score.
     *
     * @param searchQuery The user's search input.
     * @param users List of [User] objects to be ranked.
     * @return A list of users sorted by relevance to the search query.
     */
    fun rankUserSearch(searchQuery: String, users: List<User>): List<User> {
        val query = searchQuery.trim().lowercase()
        return users.map { user ->
            // Calculate how relevant each user is to the query
            val relevanceScore = calculateUserSearchScore(query, user)
            user.score = relevanceScore
            user
        }.sortedByDescending { it.score }
    }

    // region Scoring Functions

    /**
     * Calculates an engagement score for a post.
     * - Likes contribute 1.0 each
     * - Comments contribute 1.5 each
     * - Each tag contributes 0.5
     *
     * @param post The [Post] for which to calculate the score.
     * @return Engagement score as a Double.
     */
    private fun calculatePostScore(post: Post): Double {
        return post.likes * 1.0 + post.comments * 1.5 + post.tags.size * 0.5
    }

    /**
     * Calculates a relevance score for a post based on a search query.
     * - Caption match: +3.0
     * - Username match: +2.0
     * - Tag match: +1.5 per matching tag
     *
     * @param query The normalized search query.
     * @param post The [Post] to evaluate.
     * @return Relevance score as a Double.
     */
    private fun calculatePostSearchScore(query: String, post: Post): Double {
        var score = 0.0
        if (post.caption.lowercase().contains(query)) score += 3.0
        if (post.username.lowercase().contains(query)) score += 2.0
        score += post.tags.count { it.lowercase().contains(query) } * 1.5
        return score
    }

    /**
     * Calculates a relevance score for a user based on a search query.
     * - Username match: +3.0
     * - Full name match: +2.0
     * - Email match: +1.5
     * - Bio match: +1.0
     *
     * @param query The normalized search query.
     * @param user The [User] to evaluate.
     * @return Relevance score as a Double.
     */
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
