package faang.school.promotionservice.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class Skill {
    @Field(type = FieldType.Integer)
    private Long id;

    @Field(type = FieldType.Text)
    private String title;
}
