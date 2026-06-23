# Code Review Checklist

## 1. Correctness

- [ ] Logic sesuai requirement.
- [ ] Happy path berjalan.
- [ ] Negative path ditangani.
- [ ] Edge case dipikirkan.
- [ ] Status transition benar.
- [ ] Tidak ada business rule yang dilewati.

## 2. Layer Responsibility

- [ ] Controller hanya mengatur HTTP request/response.
- [ ] Service berisi business logic.
- [ ] Repository hanya akses data.
- [ ] DTO tidak berisi business logic berat.
- [ ] Security/auth helper tidak dicampur ke controller secara berlebihan.

## 3. Error Handling

- [ ] 400 digunakan untuk validation error.
- [ ] 401 digunakan untuk unauthenticated.
- [ ] 403 digunakan untuk forbidden.
- [ ] 404 digunakan untuk data tidak ditemukan.
- [ ] Error response konsisten.
- [ ] Error response memiliki correlation_id.
- [ ] Tidak ada stack trace bocor ke client.

## 4. Security & Authorization

- [ ] Endpoint protected sudah cek token/session.
- [ ] Role/access check dilakukan di backend.
- [ ] User tidak bisa akses resource milik user lain.
- [ ] Tidak percaya role dari request body.
- [ ] Token tidak ditulis ke log.
- [ ] Password tidak ditulis ke log.

## 5. Testing

- [ ] Service layer punya unit test.
- [ ] Happy path dites.
- [ ] Negative path dites.
- [ ] Access logic dites.
- [ ] Error behavior dites.
- [ ] Test name jelas.
- [ ] Assertion cukup kuat.
- [ ] Test tidak terlalu dekat dengan implementation detail.

## 6. Logging & PII

- [ ] Log menggunakan field standar seperti event_name dan correlation_id.
- [ ] Log level tepat: info, warn, error.
- [ ] Forbidden/access denied dicatat sebagai WARN.
- [ ] Unexpected error dicatat sebagai ERROR.
- [ ] Tidak log password.
- [ ] Tidak log token.
- [ ] Tidak log raw PII.
- [ ] Audit log melakukan redaction untuk field bebas.

## 7. Maintainability

- [ ] Naming jelas.
- [ ] Method tidak terlalu panjang.
- [ ] Tidak ada duplicate logic berlebihan.
- [ ] Code mudah dibaca.
- [ ] Perubahan mudah direview.
- [ ] Komentar menjelaskan trade-off jika diperlukan.

## Review Comment Example

Less helpful:

```text
Ini salah.
```

More helpful:

```text
Business rule ini lebih cocok berada di service layer agar controller tetap fokus pada HTTP flow. Ini juga membuat logic lebih mudah di-unit-test dan mengurangi risiko behavior berubah tanpa test.
```
