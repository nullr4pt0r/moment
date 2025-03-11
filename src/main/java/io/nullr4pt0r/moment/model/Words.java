package io.nullr4pt0r.moment.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "words")
public class Words {
    @Id
    private String id;
    private String word;
    private String language;
    private List<String> meanings;
    private String phonetics;
    private String englishTranslation;
    private List<String> remarks;
}
