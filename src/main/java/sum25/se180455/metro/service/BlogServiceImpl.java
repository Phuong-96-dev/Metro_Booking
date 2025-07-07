package sum25.se180455.metro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.repository.BlogRepository;

import sum25.se180455.metro.pojo.Member;

import java.time.LocalDate;
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
    public Blog createBlog(Blog blog, Long adminId) {
        blog.setPostingDate(LocalDate.now());

        // Tạo entity Member chỉ với id để tránh lỗi duplicate persist
        Member admin = new Member();
        admin.setMemberId(adminId);
        blog.setAdmin(admin);

        return blogRepository.save(blog);
    }




    @Override
    public Blog updateBlog(Long blogId, String title, String content, String img) {
        Optional<Blog> optionalBlog = blogRepository.findById(blogId);

        if (optionalBlog.isEmpty()) {
            throw new RuntimeException("Blog with id " + blogId + " not found.");
        }

        Blog blog = optionalBlog.get();
        blog.setPostingDate(LocalDate.now());
        blog.setTitle(title);
        blog.setContent(content);
        blog.setImageUrl(img);

        return blogRepository.save(blog);
    }


    @Override
    public void deleteBlog(Long blogId) {
        blogRepository.deleteById(blogId);
    }

    @Override
    public int countBlogs() {
        return (int) blogRepository.count();
    }

    @Override
    public List<Blog> findAllBlogsOrderByDateDesc() {
        return blogRepository.findAllByOrderByPostingDateDesc();
    }
}
