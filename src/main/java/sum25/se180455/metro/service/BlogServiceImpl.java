package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.repository.BlogRepository;

@Service
public class BlogServiceImpl implements BlogService{
    @Autowired
    BlogRepository blogRepository;

    @Override
    public String viewAllBlogs() {
        return blogRepository.findAll().toString();
    }

    @Override
    public String viewBlogById(Long blogId) {
        return blogRepository.findById(blogId).toString();
    }

    @Override
    public String createBlog(String title, String content, Long adminId) {
        return blogRepository.save(new Blog(title, content, adminId)).toString();
    }

    @Override
    public String updateBlog(Long blogId, String title, String content) {
        return "";
    }

    @Override
    public String deleteBlog(Long blogId) {
        return "";
    }
}
