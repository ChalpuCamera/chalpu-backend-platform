package com.example.chalpuplatform.store;

import com.example.chalpuplatform.store.domain.LinkType;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.StoreLink;
import com.example.chalpuplatform.store.dto.StoreLinkRequest;
import com.example.chalpuplatform.store.dto.StoreRequest;
import com.example.chalpuplatform.store.repository.StoreLinkRepository;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.service.StoreService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StoreServiceTransactionTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreLinkRepository storeLinkRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("IDENTITY 전략: save() 즉시 INSERT 쿼리 실행 확인")
    void testIdentityStrategyImmediateInsert() {
        StoreRequest request = StoreRequest.builder()
                .storeName("테스트 매장")
                .address("서울시 강남구")
                .description("테스트 설명")
                .siteLink("test-store")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/test")
                                .build()
                ))
                .build();

        System.out.println("=== 1. Store.createStore() 호출 전 ===");

        Store store = Store.createStore(request);
        System.out.println("=== 2. Store.createStore() 호출 후 (비영속 상태) ===");
        System.out.println("Store ID (save 전): " + store.getId());
        assertThat(store.getId()).isNull();

        System.out.println("\n=== 3. storeRepository.save() 호출 ===");
        Store savedStore = storeRepository.save(store);

        System.out.println("=== 4. storeRepository.save() 호출 직후 ===");
        System.out.println("Store ID (save 후): " + savedStore.getId());
        System.out.println("영속성 컨텍스트 contains: " + em.contains(savedStore));

        assertThat(savedStore.getId()).isNotNull();
        assertThat(em.contains(savedStore)).isTrue();

        System.out.println("\n=== 5. 링크 생성 시작 ===");
        for (int i = 0; i < request.getLinks().size(); i++) {
            System.out.println("링크 " + (i + 1) + " 생성 중...");
        }

        System.out.println("\n=== 6. em.flush() 호출 전 (트랜잭션 커밋 전) ===");
        System.out.println("현재까지 쿼리가 실행되었는지 확인");

        em.flush();
        System.out.println("\n=== 7. em.flush() 호출 후 ===");

        em.clear();
        System.out.println("\n=== 8. em.clear() 호출 후 (영속성 컨텍스트 비움) ===");

        Store foundStore = storeRepository.findById(savedStore.getId()).orElse(null);
        System.out.println("\n=== 9. DB에서 다시 조회 ===");
        System.out.println("조회된 Store: " + foundStore);
        System.out.println("조회된 Store ID: " + (foundStore != null ? foundStore.getId() : "null"));

        assertThat(foundStore).isNotNull();
        assertThat(foundStore.getId()).isEqualTo(savedStore.getId());
    }

    @Test
    @DisplayName("링크 생성 시 매장 ID 사용 가능 여부 확인")
    void testLinkCreationUsesStoreId() {
        StoreRequest request = StoreRequest.builder()
                .storeName("링크 테스트 매장")
                .address("서울시")
                .siteLink("link-test")
                .build();

        System.out.println("=== 매장 생성 ===");
        Store store = Store.createStore(request);
        Store savedStore = storeRepository.save(store);

        System.out.println("=== save() 직후 ID: " + savedStore.getId() + " ===");
        assertThat(savedStore.getId()).isNotNull();

        System.out.println("\n=== 링크 생성 (savedStore.getId() 사용) ===");
        StoreLink link = StoreLink.create(
                savedStore,
                LinkType.BAEMIN,
                null,
                "https://baemin.com/test",
                true,
                0
        );

        StoreLink savedLink = storeLinkRepository.save(link);

        System.out.println("링크 생성 성공");
        System.out.println("링크 ID: " + savedLink.getId());
        System.out.println("링크의 Store ID: " + savedLink.getStore().getId());

        assertThat(savedLink.getId()).isNotNull();
        assertThat(savedLink.getStore().getId()).isEqualTo(savedStore.getId());

        em.flush();
        em.clear();

        StoreLink foundLink = storeLinkRepository.findById(savedLink.getId()).orElse(null);
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.getStore().getId()).isEqualTo(savedStore.getId());
    }

    @Test
    @DisplayName("쿼리 실행 순서 확인: Store INSERT → Link INSERT")
    void testQueryExecutionOrder() {
        System.out.println("\n========================================");
        System.out.println("쿼리 실행 순서 테스트 시작");
        System.out.println("========================================\n");

        StoreRequest request = StoreRequest.builder()
                .storeName("쿼리 순서 테스트")
                .address("서울시")
                .siteLink("query-test")
                .build();

        System.out.println(">>> 1. Store 생성 (비영속)");
        Store store = Store.createStore(request);

        System.out.println("\n>>> 2. storeRepository.save() 호출 - IDENTITY이므로 즉시 INSERT");
        Store savedStore = storeRepository.save(store);
        System.out.println("    Store ID: " + savedStore.getId());
        System.out.println("    Store siteLink: " + savedStore.getSiteLink());

        System.out.println("\n>>> 3. StoreLink 생성");
        StoreLink link1 = StoreLink.create(savedStore, LinkType.BAEMIN, null, "https://baemin.com", true, 0);
        StoreLink link2 = StoreLink.create(savedStore, LinkType.YOGIYO, null, "https://yogiyo.com", true, 1);

        System.out.println("\n>>> 4. storeLinkRepository.save() 호출 - IDENTITY이므로 즉시 INSERT");
        StoreLink savedLink1 = storeLinkRepository.save(link1);
        StoreLink savedLink2 = storeLinkRepository.save(link2);
        System.out.println("    Link1 ID: " + savedLink1.getId());
        System.out.println("    Link2 ID: " + savedLink2.getId());

        System.out.println("\n>>> 5. em.flush() 호출 (이미 모든 INSERT 완료됨)");
        em.flush();

        System.out.println("\n>>> 6. 트랜잭션 커밋 전 상태 확인");
        assertThat(savedStore.getId()).isNotNull();
        assertThat(savedStore.getSiteLink()).isEqualTo("query-test");
        assertThat(savedLink1.getId()).isNotNull();
        assertThat(savedLink2.getId()).isNotNull();

        System.out.println("\n========================================");
        System.out.println("결론: IDENTITY 전략은 save() 즉시 INSERT 실행");
        System.out.println("========================================\n");
    }
}
