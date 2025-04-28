package faang.school.promotionservice.repository;

import faang.school.promotionservice.entity.User;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, Long> {
    @Override
    List<User> findAll();

    @Query("""
    {
      "query_string": {
        "query": "*?0*",
        "fields": ["username", "aboutMe", "skill"],
        "default_operator": "OR"
      }
    }
    """)
    List<User> universalSearch(String query);
}
