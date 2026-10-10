# Giai đoạn 0 - Nền móng: Bài tập và câu hỏi phỏng vấn

## Bài tập (tự làm, không xem lời giải)

### BT 0.1 - Sức mạnh của sealed
Trong `ModernJavaFeaturesTest`, thêm command `Withdraw(String walletId, String bankAccount, Money amount)`
(rút tiền về ngân hàng) vào `WalletCommand`.
1. Chỉ thêm vào `permits`, **chưa sửa** `fee()`. Chạy `./mvnw test` và đọc lỗi compile.
2. Viết rule phí: rút dưới 2.000.000 VND phí 3.300 VND, từ 2.000.000 trở lên miễn phí.
   Viết test cho đúng mốc biên (1.999.999 / 2.000.000).
3. Câu hỏi tự trả lời: nếu `fee()` có nhánh `default ->` thì bước 1 sẽ khác thế nào? Điều đó nguy hiểm ra sao?

### BT 0.2 - Flyway checksum
1. Chạy `docker compose up -d`, chạy app (`./mvnw spring-boot:run`).
2. Tắt app, **sửa** `V1__create_feature_flag.sql` (ví dụ đổi `VARCHAR(255)` thành `VARCHAR(500)`), chạy lại.
   Đọc lỗi. Mở bảng `flyway_schema_history` xem cột `checksum`.
3. Hoàn tác thay đổi đó. Làm đúng cách: tạo `V2__...sql` để (a) đổi độ dài cột và (b) seed một flag
   `transfer.locking-strategy` với description. Thêm assertion vào `InfrastructureSmokeIT`.
4. Tự trả lời: `flyway repair` làm gì? Vì sao không nên chạy nó trên production để "chữa cháy" việc sửa migration?

### BT 0.3 - Tính phần trăm cho tiền
Thêm vào `Money` phương thức `Money percentage(long basisPoints, RoundingMode mode)`
(1 basis point = 0,01%; ví dụ phí 1,1% = 110 bps).
- Không dùng `double` ở bất kỳ bước nào.
- Test: 10.000 VND × 110 bps = 110 VND; 999 VND × 110 bps với `HALF_UP`, `DOWN`, `HALF_EVEN`.
- Test số lớn: `Long.MAX_VALUE` × 10.000 bps phải ném lỗi hoặc ra đúng kết quả, **không được** ra số âm.
- Ghi vào LEARNING_LOG: bạn chọn rounding mode mặc định nào cho phí thu của user, cho cashback trả user, và vì sao.

---

## 5 câu hỏi phỏng vấn
Tự trả lời trước (nói to hoặc viết ra), rồi mới mở gợi ý.

**Q1. Vì sao không dùng `double` cho tiền? Giữa `long` (đơn vị nhỏ nhất) và `BigDecimal` thì chọn thế nào?**
<details><summary>Gợi ý</summary>

- `double` là phân số nhị phân (IEEE 754): 0.1 không biểu diễn chính xác được, sai số cộng dồn qua nhiều phép tính.
  Vượt 2^53 thì mất cả số nguyên.
- `long` minor units: nhanh, 8 byte, so sánh/cộng trừ đơn giản, map thẳng sang `BIGINT`. Hợp với
  tiền tệ có scale cố định (VND scale 0, USD scale 2). Phải tự lo tràn số (`Math.addExact`).
- `BigDecimal`: tùy ý độ chính xác, có `RoundingMode`, hợp với lãi suất, tỷ giá, đa tiền tệ.
  Bẫy: `equals` so sánh cả scale, `new BigDecimal(double)`, object bất biến nên tốn allocation, GC nhiều hơn.
- Điểm cộng khi trả lời: nói được *rounding nằm ở đâu và ai chịu phần lẻ* (về phía user hay công ty).
</details>

**Q2. Vì sao dùng Flyway/Liquibase thay vì `ddl-auto=update`? Làm sao đổi schema bảng lớn mà không downtime?**
<details><summary>Gợi ý</summary>

- `update` không xóa/đổi tên cột, không migrate dữ liệu, không review được, không có lịch sử, không rollback được.
  Mỗi môi trường có thể "trôi" schema khác nhau.
- Migration có version + checksum, được review như code, lặp lại được trên mọi môi trường.
- Zero-downtime: **expand/contract**. Thêm cột mới (nullable), code ghi cả hai, backfill theo batch,
  chuyển đọc sang cột mới, rồi bản phát hành sau mới xóa cột cũ. Với MySQL bảng lớn: `ALGORITHM=INSTANT/INPLACE`,
  hoặc gh-ost / pt-online-schema-change, để ý metadata lock (MDL) chặn mọi truy vấn khi ALTER chờ lock.
</details>

**Q3. Sealed + pattern matching khác gì đa hình (polymorphism) thông thường hay Visitor pattern? Khi nào dùng cái nào?**
<details><summary>Gợi ý</summary>

- Đa hình (method trên từng lớp): dễ thêm **kiểu** mới, khó thêm **thao tác** mới (phải sửa mọi lớp).
- Sealed + switch: dễ thêm **thao tác** mới (viết switch mới), thêm kiểu mới thì compiler chỉ ra mọi chỗ cần sửa.
  Đây là "expression problem".
- Visitor là cách làm sealed+switch thời chưa có pattern matching: nhiều boilerplate, double dispatch.
- Dùng sealed khi tập kiểu đóng và ổn định (kết quả thanh toán: `Success | Failed | Pending`; các loại command),
  và logic nằm ngoài dữ liệu (phí, mapping, serialization).
</details>

**Q4. Có dùng record làm JPA entity được không? Vì sao?**
<details><summary>Gợi ý</summary>

- Không nên/không được làm `@Entity`: record là `final` (Hibernate không tạo proxy để lazy load được),
  field `final` (không có no-arg constructor, không set được sau khi load), không mutable để dirty checking.
- Được dùng làm: DTO/projection (`SELECT new ...Record(...)` hoặc interface/record projection của Spring Data),
  `@Embeddable` (Hibernate 6.2+ hỗ trợ record embeddable), value object, command, event.
- Liên quan: `equals/hashCode` của entity nên dựa trên id (cẩn thận id null trước khi persist) hoặc natural key.
</details>

**Q5. Vì sao test với Testcontainers thay vì H2 in-memory? Có nhược điểm gì và giảm thiểu thế nào?**
<details><summary>Gợi ý</summary>

- H2 "MySQL mode" chỉ giả lập cú pháp. Locking, MVCC, isolation, gap lock, deadlock detection, optimizer,
  collation, kiểu JSON/hàm đặc thù đều khác. Test xanh trên H2 vẫn có thể lỗi trên MySQL thật,
  đúng ở những chỗ quan trọng nhất với hệ thống tiền.
- Nhược điểm: chậm (khởi động container), cần Docker trong CI.
- Giảm thiểu: dùng chung container qua Spring context cache (hoặc singleton container pattern),
  giữ cấu hình test giống nhau để tránh tạo context mới, `withReuse(true)` khi chạy local,
  tách unit test thuần (nhanh) với IT (chậm), chạy song song có kiểm soát.
</details>
