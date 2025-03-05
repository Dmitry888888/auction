package auction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authorities")
// если этой таблицы не будет не заходит логин - стандартная схема https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/jdbc.html
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String authority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private CustomUserDetails user;

    public Authority(String username, String authority) {
        this.username = username;
        this.authority = authority;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

}
