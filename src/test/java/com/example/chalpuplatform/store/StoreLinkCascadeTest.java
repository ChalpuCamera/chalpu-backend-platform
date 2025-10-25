package com.example.chalpuplatform.store;

import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.store.domain.LinkType;
import com.example.chalpuplatform.store.domain.Store;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StoreLinkCascadeTest {

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreLinkRepository storeLinkRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("매장 생성 시 링크가 CASCADE로 자동 저장된다")
    void testCreateStoreWithLinksCascade() {
        StoreRequest request = StoreRequest.builder()
                .storeName("테스트 매장")
                .address("서울시 강남구")
                .description("테스트 설명")
                .siteLink("test-store")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/test")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.INSTAGRAM)
                                .url("https://instagram.com/test")
                                .build()
                ))
                .build();

        Store store = Store.createStore(request);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(foundStore.getSiteLink()).isEqualTo("test-store");
        assertThat(foundStore.getLinks()).hasSize(2);
        assertThat(foundStore.getLinks().get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(foundStore.getLinks().get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(foundStore.getLinks().get(0).getLinkType()).isEqualTo(LinkType.BAEMIN);
        assertThat(foundStore.getLinks().get(1).getLinkType()).isEqualTo(LinkType.INSTAGRAM);
    }

    @Test
    @DisplayName("매장 수정 시 기존 링크가 DELETE되고 새 링크가 INSERT된다")
    void testUpdateStoreLinksCascadeDelete() {
        StoreRequest createRequest = StoreRequest.builder()
                .storeName("원본 매장")
                .address("서울시")
                .siteLink("original-store")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/original")
                                .build()
                ))
                .build();

        Store store = Store.createStore(createRequest);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Long originalLinkId = savedStore.getLinks().get(0).getId();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();

        StoreRequest updateRequest = StoreRequest.builder()
                .storeName("수정된 매장")
                .address("서울시")
                .siteLink("updated-store")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.YOGIYO)
                                .url("https://yogiyo.com/updated")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.INSTAGRAM)
                                .url("https://instagram.com/updated")
                                .build()
                ))
                .build();

        foundStore.updateStore(updateRequest);

        em.flush();
        em.clear();

        assertThat(storeLinkRepository.findById(originalLinkId)).isEmpty();

        Store updatedStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(updatedStore.getSiteLink()).isEqualTo("updated-store");
        assertThat(updatedStore.getLinks()).hasSize(2);
        assertThat(updatedStore.getLinks().get(0).getLinkType()).isEqualTo(LinkType.YOGIYO);
        assertThat(updatedStore.getLinks().get(1).getLinkType()).isEqualTo(LinkType.INSTAGRAM);
    }

    @Test
    @DisplayName("orphanRemoval로 links.clear() 시 모든 링크가 DELETE된다")
    void testOrphanRemovalDeletesLinks() {
        StoreRequest request = StoreRequest.builder()
                .storeName("테스트 매장")
                .address("서울시")
                .siteLink("test-orphan")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/test")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.INSTAGRAM)
                                .url("https://instagram.com/test")
                                .build()
                ))
                .build();

        Store store = Store.createStore(request);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Long link1Id = savedStore.getLinks().get(0).getId();
        Long link2Id = savedStore.getLinks().get(1).getId();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        foundStore.getLinks().clear();

        em.flush();
        em.clear();

        assertThat(storeLinkRepository.findById(link1Id)).isEmpty();
        assertThat(storeLinkRepository.findById(link2Id)).isEmpty();

        Store storeAfterClear = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(storeAfterClear.getLinks()).isEmpty();
    }

    @Test
    @DisplayName("siteLink이 중복되면 DB 제약조건 위반으로 예외가 발생한다")
    void testSiteLinkDuplicationValidation() {
        StoreRequest request1 = StoreRequest.builder()
                .storeName("매장1")
                .address("서울시")
                .siteLink("duplicate-site")
                .build();

        storeService.createStore(request1);

        StoreRequest request2 = StoreRequest.builder()
                .storeName("매장2")
                .address("부산시")
                .siteLink("duplicate-site")
                .build();

        assertThatThrownBy(() -> storeService.createStore(request2))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("링크 displayOrder가 배열 인덱스 순서대로 설정된다")
    void testDisplayOrderMatchesArrayIndex() {
        StoreRequest request = StoreRequest.builder()
                .storeName("테스트 매장")
                .address("서울시")
                .siteLink("test-order")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.INSTAGRAM)
                                .url("https://instagram.com/1")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/1")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.YOGIYO)
                                .url("https://yogiyo.com/1")
                                .build()
                ))
                .build();

        Store store = Store.createStore(request);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();

        for (int i = 0; i < foundStore.getLinks().size(); i++) {
            assertThat(foundStore.getLinks().get(i).getDisplayOrder()).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("링크 없이 매장을 생성할 수 있다")
    void testCreateStoreWithoutLinks() {
        StoreRequest request = StoreRequest.builder()
                .storeName("링크 없는 매장")
                .address("서울시")
                .siteLink("no-links")
                .build();

        Store store = Store.createStore(request);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(foundStore.getSiteLink()).isEqualTo("no-links");
        assertThat(foundStore.getLinks()).isEmpty();
    }

    @Test
    @DisplayName("매장 수정 시 links=null이면 기존 링크가 유지된다")
    void testUpdateStoreWithNullLinksKeepsExisting() {
        StoreRequest createRequest = StoreRequest.builder()
                .storeName("원본 매장")
                .address("서울시")
                .siteLink("keep-links")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/keep")
                                .build()
                ))
                .build();

        Store store = Store.createStore(createRequest);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();

        StoreRequest updateRequest = StoreRequest.builder()
                .storeName("수정된 매장")
                .address("부산시")
                .siteLink("keep-links")
                .links(null)
                .build();

        foundStore.updateStore(updateRequest);

        em.flush();
        em.clear();

        Store updatedStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(updatedStore.getLinks()).hasSize(1);
        assertThat(updatedStore.getLinks().get(0).getUrl()).isEqualTo("https://baemin.com/keep");
    }

    @Test
    @DisplayName("매장 수정 시 빈 배열을 보내면 모든 링크가 삭제된다")
    void testUpdateStoreWithEmptyArrayDeletesAllLinks() {
        StoreRequest createRequest = StoreRequest.builder()
                .storeName("원본 매장")
                .address("서울시")
                .siteLink("to-be-deleted")
                .links(List.of(
                        StoreLinkRequest.builder()
                                .linkType(LinkType.BAEMIN)
                                .url("https://baemin.com/delete")
                                .build(),
                        StoreLinkRequest.builder()
                                .linkType(LinkType.YOGIYO)
                                .url("https://yogiyo.com/delete")
                                .build()
                ))
                .build();

        Store store = Store.createStore(createRequest);
        Store savedStore = storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findById(savedStore.getId()).orElseThrow();

        StoreRequest updateRequest = StoreRequest.builder()
                .storeName("수정된 매장")
                .address("서울시")
                .siteLink("to-be-deleted")
                .links(List.of())
                .build();

        foundStore.updateStore(updateRequest);

        em.flush();
        em.clear();

        Store updatedStore = storeRepository.findById(savedStore.getId()).orElseThrow();
        assertThat(updatedStore.getLinks()).isEmpty();
    }

    @Test
    @DisplayName("siteLink로 매장을 조회할 수 있다")
    void testFindBySiteLink() {
        StoreRequest request = StoreRequest.builder()
                .storeName("사이트 링크 매장")
                .address("서울시")
                .siteLink("findable-store")
                .build();

        Store store = Store.createStore(request);
        storeRepository.save(store);

        em.flush();
        em.clear();

        Store foundStore = storeRepository.findBySiteLink("findable-store").orElseThrow();
        assertThat(foundStore.getStoreName()).isEqualTo("사이트 링크 매장");
    }
}
