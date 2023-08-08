package orgaplan.beratung.kreditunterlagen.model;

import orgaplan.beratung.kreditunterlagen.Types;
import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @Column(name = "document_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Types.DocumentType documentType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;
}
