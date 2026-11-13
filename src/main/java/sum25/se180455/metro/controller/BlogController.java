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

    @GetMapping("/blog")
    public String blogPage(Model model) {
        model.addAttribute("blogs", blogService.findAllBlogsOrderByDateDesc());
        return "blog"; // trỏ tới file feedback.html
    }
    // Blog page


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

    @PostMapping("/adminManage/deleteBlog/{id}")
    public String deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id);
        return "redirect:/adminManage/manageBlog";
    }









}
