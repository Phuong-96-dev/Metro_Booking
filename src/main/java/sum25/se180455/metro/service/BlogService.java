package sum25.se180455.metro.service;

public interface BlogService {
    // view all
    public String viewAllBlogs();
    // view by id
    public String viewBlogById(Long blogId);
    // create
    public String createBlog(String title, String content, Long adminId);
    // update
    public String updateBlog(Long blogId, String title, String content);
    // delete
    public String deleteBlog(Long blogId);
}
