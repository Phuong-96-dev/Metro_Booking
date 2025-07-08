package sum25.se180455.metro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sum25.se180455.metro.pojo.Blog;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog,Long> {
    List<Blog> findAllByOrderByPostingDateDesc();
    // count blogs
}
