package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.repository.BlogRepository;

import sum25.se180455.metro.pojo.Member;

import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {
    @Autowired
    BlogRepository blogRepository;

    @Override
    public List<Blog> viewAllBlogs() {
        return blogRepository.findAll();
    }

    @Override
    public Optional<Blog> viewBlogById(Long blogId) {
        return blogRepository.findById(blogId);
    }

    @Override
    public Blog createBlog(String title, String content, String img, Member adminId) {
        return blogRepository.save(new Blog(title, content, img, adminId));
    }

    @Override
    public Blog updateBlog(Long blogId, String title, String content, String img) {
        Optional<Blog> optionalBlog = blogRepository.findById(blogId);

        if (optionalBlog.isEmpty()) {
            throw new RuntimeException("Blog with id " + blogId + " not found.");
        }

        Blog blog = optionalBlog.get();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setImageUrl(img);

        return blogRepository.save(blog);
    }


    @Override
    public void deleteBlog(Long blogId) {
        blogRepository.deleteById(blogId);
    }
}
