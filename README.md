# Jpashop-Study
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'com.h2database:h2'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**H2**

jdbc:h2:~/jpashop -> 초기에 파일로 접근(최소 한번, 세션키 유지한 상태로 실행)

-> ~/jpashop.mv.db 파일 생성

jdbc:h2:tcp://localhost/~/jpashop -> 이후에 tcp로 접근

```yaml
spring:
  datasource:
    url: jdbc:h2:tcp://localhost/~/jpashop;MVCC=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        show_sql: true
        format_sql: true
logging:
  level:
    org.hibernate.SQL: debug
```

**JPA**

persistence.xml, LocalContainerEntityManagerFactoryBean 설정 없이 Boot에서 자동 설정해준다.

**설계**

![image-20210318201644036](C:\Users\USER\AppData\Roaming\Typora\typora-user-images\image-20210318201644036.png)

![image-20210318201716328](C:\Users\USER\AppData\Roaming\Typora\typora-user-images\image-20210318201716328.png)

![image-20210318202014026](C:\Users\USER\AppData\Roaming\Typora\typora-user-images\image-20210318202014026.png)

- 회원 - 주문 : 다대일 양방향 -> 주인관계 정해야함 -> 주문이 외래키 보유하므로 주문
- 주문상품 - 주문 : 다대일 양방향 관계 -> 주문상품이 외래키보유
- 주문상품 - 상품 : 다대일 단방향
- 주문 - 배송 : 일대일 단방향
- 카테고리와 상품 : 다대다 관계

**주의점**

- @Getter는 열어두고, @Setter는 꼭 필요한 경우에만 사용하는것을 추천
  (유지보수를 위해 변경 비즈니스 메서드를 별도로 만드는게 좋다)

- 값 타입은 변경불가능하게 설계해야한다.
  @Setter는 제거하고, 생성자 부여
  @Embeddable은 리플랙션/프록시 같은 기술 사용할 수 있도록, 기본 생성자 필요(protected로 선언하는것을 추천)

- 모든 연관관계는 지연로딩(LAZY)으로 (@ManyToOne, @OneToOne 즉시로딩이 디폴트이므로 주의)
  entitymanager 사용으로 가져오면 상관없지만,
  JPQL 사용시 N+1문제 발생 -> fetch join, 엔티티 그래프 활용

- 컬렉션 필드는 필드에서 초기에 직접 초기화하자.

  ```java
  @OneToMany(mappedBy = "member")
  private List<Order> orders = new ArrayList<>();
  ```

  null safe
  영속성 관리를 위해 컬렉션 필드를 감싸서 추적한다. ( 초기화이후 변경되지 않도록 하자 )

- 엔티티 필드명 -> 테이블 컬럼명 (캐멀 케이스 사용) -> 설정으로 변경은 가능

- ```java
  @OneToMany(mappedBy = &quot;order&quot;, cascade = CascadeType.ALL) // 연관 테이블을 persist 한번으로 저장가능
  private List&lt;OrderItem&gt; orderItems = new ArrayList&lt;&gt;();ㅇ
  ```

- 양방향 연관관계 편의 메서드

  ```java
  public class Order
      ...
  public void setMember(Member member) {
  	this.member = member;
  	member.getOrders().add(this);
  }
  public void addOrderItem(OrderItem orderItem) {
  	orderItems.add(orderItem);
  	orderItem.setOrder(this);
  }
  public void setDelivery(Delivery delivery) {
  	this.delivery = delivery;
  	delivery.setOrder(this);
  }
  ```



의존성 주입은

1. 필드 주입 (비추)

   ```java
   @Autowired
   private MemberRepository memberRepository;
   ```

2. 생성자 주입

   ```java
   private final MemberRepository memberRepository;
   @Autowired
   public MemberService(MemberRepository memberRepository) {
   	this.memberRepository = memberRepository;
   } // 의존성 주입 교체 가능, final으로 컴파일 시점 오류 검사 가능
   ```

3. ```java
   @RequiredArgsConstructor // final 필드 생성자 생성
   public class MemberService {
       private final MemberRepository memberRepository;
   ```

4. ```java
   @Repository
   @RequiredArgsConstructor
   public class MemberRepository {
       private final EntityManager em; 
       // 부트에선 @PersistenceContext 대신 @Autowired로 인젝션 가능하기 때문에 이렇게 선언가능
   ```


**테스트**

src/test/resources/appication.yml 이 있다면, 테스트환경시 이쪽을 참조

또, DB properties를 비워놓으면 default로 메모리 DB로 실행

@Transactional -> Test에선 실행 뒤 롤백이 default

@NoArgsConstructor(access = AccessLevel.PROTECTED) -> 다른 생성자로인한 코드 분산 막기

도메인 모델 패턴(JPA추천) - 서비스 계층은 단순히 엔티티에 필요한 요청을 위임, 엔티티가 비즈니스 로직 가짐

드랜잭션 스크립트 패턴 - 반대로 엔티티에 비즈니스 로직 없고, 서비스가 비즈니스 로직을 처리



**thymeleaf**

@Valid + @NotEmpty(message = "회원 이름은 필수입니다.") + BindingResult -> 백단의 에러를 뷰까지 끌어감

```html
<input type="text" th:field="*{name}" class="form-control" placeholder="이름을 입력하세요"
                   th:class="${#fields.hasErrors('name')}? 'form-control fieldError' : 'form-control'">
<p th:if="${#fields.hasErrors('name')}" th:errors="*{name}">Incorrect date</p>
```

? -> null 처리

```html
<!-- fragments 사용 (like include) -->
<div th:replace="fragments/footer :: footer"></div>
```

```html
<!-- 반복 -->
<tr th:each="member : ${members}">
    <td th:text="${member.id}"></td>
    <td th:text="${member.name}"></td>
    <td th:text="${member.address?.city}"></td>
    <td th:text="${member.address?.street}"></td>
    <td th:text="${member.address?.zipcode}"></td>
</tr>
```

```html
<!-- 가변 uri -->
<a href="#" th:href="@{/items/{id}/edit (id=${item.id})}" class="btn btn-primary" role="button">수정</a>
```

```html
<!-- enum 타입 뿌리기 -->
<option th:each="status : ${T(jpabook.jpashop.domain.OrderStatus).values()}"
        th:value="${status}"
        th:text="${status}">option
```

**변경 감지 & 병합**

준영속성 엔티티 - 영속성 컨텍스트가 더이상 관리하지 않는 엔티티
(DB에 한 번 저장되었다가, 다시 만들어진 식별자(id)가진 엔티티 포함)

-> 수정 방법 2가지

- 변경 감지 : @Transactional + find/set ( set보단 의미있는 메서드 사용)
- 병합(merge) : em.merge()
- 내부적으론 같은 동작을 하지만, 병합시 null 업데이트 위험이 있으므로
  변경감지로 원하는 필드만 교체하는것을 추천
- -> Dto는 컨트롤러 단에서만 사용하도록 하며, 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회후, 변경감지 사용

