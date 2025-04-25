package tn.esprit.entities;

public class Flamme {

    private Long id;
    private Long userId;
    private int count;

    public Flamme() {}

    public Flamme(Long userId, int count) {
        this.userId = userId;
        this.count = count;
    }

    // ✅ Getter / Setter ID
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // ✅ Getter / Setter UserId
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // ✅ Getter / Setter Count
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Flamme{" +
                "id=" + id +
                ", userId=" + userId +
                ", count=" + count +
                '}';
    }
}
