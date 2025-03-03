package model.mongo;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Credentials {
    private ObjectId id;
    private String username;
    private String password;
}
