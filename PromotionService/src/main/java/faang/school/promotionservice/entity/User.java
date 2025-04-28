package faang.school.promotionservice.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "user")
@Setting(settingPath = "elasticsearch/settings.json")
public class User {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String username;

    @Field(type = FieldType.Text, analyzer = "english")
    private String aboutMe;

    @Field(type = FieldType.Nested, includeInParent = true)
    private Skill skill;

    @Field(type = FieldType.Integer)
    private int priority;
}
