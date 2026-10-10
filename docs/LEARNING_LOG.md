# Learning Log - MiniPay

Mỗi giai đoạn ghi: đã học gì, quyết định thiết kế, đánh đổi, và những chỗ bị vấp.

---

## Giai đoạn 0 - Nền móng (2026-10-09)

### Đã học
- **Records**: kiểu dữ liệu bất biến. Compiler tự sinh constructor, accessor, `equals`/`hashCode`/`toString`.
  Compact constructor là chỗ đặt validation (`Deposit` từ chối số tiền <= 0).
- **Sealed interface + switch pattern matching**: compiler biết hết các lớp con, nên `switch` không cần
  `default`. Thêm một lớp con mới thì mọi `switch` chưa xử lý nó sẽ không compile được.
  Lỗi bị đẩy từ runtime về compile time.
- **Record pattern + guard** (`case Transfer(var f, var t, Money a) when ...`): tách field ngay trong
  `case`. Thứ tự case có ý nghĩa: case có guard phải đứng trước case chung.
- **Tiền**: `0.1 + 0.2 != 0.3` với double; `double` mất chính xác khi vượt 2^53;
  `new BigDecimal("1.0").equals(new BigDecimal("1.00"))` là `false` (so sánh cả scale), phải dùng `compareTo`.
  `new BigDecimal(0.1)` giữ nguyên sai số nhị phân, nên dùng `BigDecimal.valueOf` hoặc constructor String.
- **Flyway**: bảng `flyway_schema_history` lưu version và checksum của từng migration. Sửa file đã chạy
  thì app không khởi động được.
- **Profiles**: `application.yml` (chung) + `application-{local,prod}.yml`. Profile `prod` cố tình
  không có giá trị mặc định để thiếu biến môi trường thì app chết ngay khi khởi động (fail fast).
- **Testcontainers + `@ServiceConnection`**: container khởi động trên port ngẫu nhiên, Spring đọc port đó
  và tạo `ConnectionDetails` thay cho `spring.datasource.url`. Container được dùng chung nhờ
  Spring test context cache (cùng cấu hình thì cùng context).
- **Surefire vs Failsafe**: `*Test` chạy ở phase `test`, `*IT` chạy ở `integration-test`/`verify`.
  Failsafe chạy hết IT rồi mới báo lỗi, nên container vẫn được dọn sạch.

### Quyết định thiết kế (chi tiết trong `docs/adr/`)
| ADR | Quyết định | Đánh đổi |
|-----|-----------|----------|
| 0001 | Modular monolith, package theo feature | Ranh giới module chưa được tool kiểm tra |
| 0002 | `Money` = `long` VND | Tính phần trăm cần quy tắc làm tròn tường minh; chỉ hỗ trợ một tiền tệ |
| 0003 | Flyway quản lý schema, `ddl-auto=validate` | Phải viết SQL tay (cũng chính là thứ cần học) |
| 0004 | Testcontainers, không dùng H2 | Test chậm hơn, cần Docker |
| 0005 | Không Lombok | Entity dài hơn |

Các thiết lập cố ý:
- `open-in-view: false`: tránh lazy-load ngầm khi serialize JSON và giữ connection DB suốt request.
- MySQL `--innodb-print-all-deadlocks=ON`: chuẩn bị cho Giai đoạn 3.
- Kafka `auto.create.topics.enable=false`: topic phải khai báo trong code. Gõ sai tên topic thì báo lỗi
  thay vì lặng lẽ tạo topic mới.
- Producer `acks=all` + `enable.idempotence=true`: broker loại bỏ bản ghi trùng khi producer retry.
- Hikari `connection-timeout=3000`: thà fail nhanh còn hơn để request xếp hàng vô hạn khi pool cạn
  (Giai đoạn 7 sẽ mô phỏng sự cố này).

### Chỗ bị vấp (bài học thật)
1. `java -version` báo 21 nhưng `mvn -v` báo **17**. `java` trên PATH lấy từ `javapath` của Oracle,
   còn Maven dùng `JAVA_HOME`. Đã thêm `maven-enforcer-plugin` (`requireJavaVersion [21,)`) để build
   báo lỗi rõ ràng thay vì lỗi khó hiểu `release version 21 not supported`.
2. palantir-java-format bản mặc định của Spotless **không parse được record pattern** của Java 21.
   Tooling luôn chậm hơn phiên bản ngôn ngữ, nên phải pin phiên bản formatter.
