package sum25.se180455.metro.service;

import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.pojo.Member;

import java.util.List;
import java.util.Optional;

public interface BlogService {
    // view all
    public List<Blog> viewAllBlogs();
    // view by id
    public Optional<Blog> viewBlogById(Long blogId);
    // create
    public Blog createBlog(Blog blog, Long admin_id);
    // update
    public Blog updateBlog(Long blogId, String title, String content, String img);
    // delete
    public void deleteBlog(Long blogId);

    // count
    public int countBlogs();
    // view
    public List<Blog> findAllBlogsOrderByDateDesc();

    Blog createBlog(Blog blog);
}
