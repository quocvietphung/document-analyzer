package orgaplan.beratung.kreditunterlagen.model;

import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import orgaplan.beratung.kreditunterlagen.Types;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_requests")
@Getter
@Setter
public class CreditRequest {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "kredit_typ", length = 255, nullable = false)
    @Enumerated(EnumType.STRING)
    private Types.KreditTyp kreditTyp;

    @Column(name = "kredit_link", length = 255)
    private String kreditLink;

    @Column(name = "betrag", precision = 15, scale = 2, nullable = false)
    private BigDecimal betrag;

    @Column(name = "laufzeit", nullable = false)
    private Integer laufzeit;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
