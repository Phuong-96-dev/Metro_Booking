package sum25.se180455.metro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sum25.se180455.metro.pojo.Blog;
import sum25.se180455.metro.service.BlogService;
import sum25.se180455.metro.pojo.Member;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BlogController {
    @Autowired
    private BlogService blogService;


    // Blog page
    @GetMapping("/feedback")
    public String blogPage(Model model) {
        model.addAttribute("blogs", blogService.findAllBlogsOrderByDateDesc());
        return "member/feedback"; // trỏ tới file feedback.html
    }

    // Manage blog page
    @GetMapping("/adminManage/manageBlog")
    public String manageBlogPage(Model model) {
        List<Blog> blogs = blogService.viewAllBlogs();
        model.addAttribute("blogs", blogs);
        return "adminManage/manageBlog";
    }

    @PostMapping("/adminManage/addBlog")
    public String addBlog(@ModelAttribute Blog blog, HttpSession session) {
        Member admin = (Member) session.getAttribute("member");
        if (admin != null) {
            blogService.createBlog(blog, admin.getMemberId());
        }
        // chuyển hướng về GET /adminManage/manageBlog
        return "redirect:/adminManage/manageBlog";
    }

    @PostMapping("/adminManage/updateBlog")
    public String updateBlog(@RequestParam Long blogId,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam String imageUrl) {
        blogService.updateBlog(blogId, title, content, imageUrl);
        return "redirect:/adminManage/manageBlog";
    }

    @GetMapping("/adminManage/deleteBlog/{id}")
    public String deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return "redirect:/adminManage/manageBlog";
    }




    // Trang gửi bài viết từ người dùng (hoặc phản hồi)
    @GetMapping("/feedback/send")
    public String sendBlogForm() {
        return "feedback"; // dùng chung feedback.html
    }

    // Xử lý gửi bài viết từ người dùng
    @PostMapping("/feedback/send")
    public String sendBlog(@RequestParam String title,
                           @RequestParam String content) {

        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setPostingDate(LocalDate.now());
        blog.setAdmin(null); // Không phải bài admin

        blogService.createBlog(blog); // không truyền adminId

        return "redirect:/myTicket?success";
    }



}
