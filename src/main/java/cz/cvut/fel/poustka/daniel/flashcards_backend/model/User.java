package cz.cvut.fel.poustka.daniel.flashcards_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "user_account")
@NamedQueries({
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email")
})
public class User extends AbstractEntity
{
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Date registrationDate;

    @Column(nullable = false)
    private boolean isActivated;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private VerificationToken verificationToken;

    @JsonIgnore
    @OneToOne(mappedBy = "user", orphanRemoval = true, optional = true, cascade = CascadeType.ALL)
    private PasswordResetToken passwordResetToken;

    /**
     * User frontend customization configuration like left/right card rotation, colorTheme,...
     * Its JSON object saved as String.
     */
    @Column
    private String preferences;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<LinkedCollection> linkedCollectionList;

    @JsonIgnore
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    private List<CardCollection> ownedCollectionList;

    public User()
    {
        linkedCollectionList = new ArrayList<>();
        ownedCollectionList = new ArrayList<>();
        this.isActivated = false;
        this.role = Role.USER;
        this.preferences = "{}";
    }

    public User(String email, String username, String password, Date registrationDate, List<LinkedCollection> linkedCollectionList)
    {
        ownedCollectionList = new ArrayList<>();

        if (linkedCollectionList != null)
            this.linkedCollectionList = linkedCollectionList;
        else
            this.linkedCollectionList = new ArrayList<>();

        this.role = Role.USER;
        this.preferences = "{}";
        this.isActivated = false;

        this.email = email;
        this.username = username;
        this.password = password;
        this.registrationDate = registrationDate;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Date getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public Role getRole()
    {
        return role;
    }

    public void setRole(Role role)
    {
        this.role = role;
    }

    public void encodePassword(PasswordEncoder encoder)
    {
        this.password = encoder.encode(password);
    }

    public void erasePassword()
    {
        this.password = null;
    }

    public boolean isAdmin()
    {
        return Role.ADMIN.equals(this.role);
    }

    public String getPreferences()
    {
        return preferences;
    }

    public void setPreferences(String preferences)
    {
        this.preferences = preferences;
    }

    public List<LinkedCollection> getLinkedCollectionList()
    {
        return linkedCollectionList;
    }

    public void setLinkedCollectionList(List<LinkedCollection> linkedCollectionList)
    {
        this.linkedCollectionList = linkedCollectionList;
    }

    public List<CardCollection> getOwnedCollectionList()
    {
        return ownedCollectionList;
    }

    public void setOwnedCollectionList(List<CardCollection> ownedCollectionList)
    {
        this.ownedCollectionList = ownedCollectionList;
    }

    public boolean getIsActivated()
    {
        return this.isActivated;
    }

    public void setIsActivated(boolean activated)
    {
        this.isActivated = activated;
    }

    public VerificationToken getVerificationToken()
    {
        return verificationToken;
    }

    public void setVerificationToken(VerificationToken verificationToken)
    {
        this.verificationToken = verificationToken;
    }

    public PasswordResetToken getPasswordResetToken()
    {
        return passwordResetToken;
    }

    public void setPasswordResetToken(PasswordResetToken passwordResetToken)
    {
        this.passwordResetToken = passwordResetToken;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return getId().equals(user.getId());
    }

    @Override
    public String toString()
    {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled='" + isActivated + '\'' +
                '}';
    }
}
