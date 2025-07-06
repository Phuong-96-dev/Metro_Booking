package sum25.se180455.metro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import sum25.se180455.metro.service.BlogService;

@Controller
public class BlogController {
    @Autowired
    private BlogService blogService;


    // Blog page
    @GetMapping("/blog")
    public String blogPage() {
        return "blog";
    }

    // Manage blog page
    @GetMapping("/manageBlog")
    public String manageBlogPage() {
        return "manageBlog";
    }

}
