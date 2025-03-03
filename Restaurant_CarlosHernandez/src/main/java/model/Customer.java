package model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customers")

@NamedQueries({ @NamedQuery(name = "HQL_GET_ALL_CUSTOMER",
        query = "from Customer") })
public class Customer {
    @Id
    private Integer id;
    private String first_name;
    private String last_name;
    private String email;
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @OneToOne(cascade = CascadeType.REMOVE)
    @MapsId
    @JoinColumn(name = "id")
    private Credentials credentials;

    public Customer(Integer id, String first_name, String last_name, String email, String phone, LocalDate dob) {
        this.id=id;
        this.first_name=first_name;
        this.last_name=last_name;
        this.email=email;
        this.phone=phone;
        this.dob=dob;
    }

    @Override
    public String toString() {
        return id + ";" + first_name + ";" + last_name + ";" + email + ";" + phone + ";" + dob.toString();
    }

}