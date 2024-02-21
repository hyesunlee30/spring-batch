package com.bdlee.spring.batch.querydsl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Slf4j
public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {
    protected final Map<String, Object> jpaPropertyMap = new HashMap<>();
    protected EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    protected boolean transacted = true;//default value

    protected QuerydslPagingItemReader() {
        setName(ClassUtils.getShortName(QuerydslPagingItemReader.class));
    }

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory,
                                    int pageSize,
                                    Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this();
        this.entityManagerFactory = entityManagerFactory;
        this.queryFunction = queryFunction;
        setPageSize(pageSize);
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();

        try {
            entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap);
            if (entityManager == null) {
                throw new DataAccessResourceFailureException("Unable to obtain an EntityManager");
            }
            log.info("doOpen: EntityManager opened successfully");
        } catch (Exception e) {
            log.error("Error while opening EntityManager", e);
            throw e; // 예외를 던져서 상위로 전파하도록 수정
        }
    }


    // QuerydslPagingItemReader 클래스의 doReadPage 메서드
    @Override
    protected void doReadPage() {
        clearIfTransacted();

        JPAQuery<T> query = createQuery()
                .offset(getPage() * getPageSize())
                .limit(getPageSize());

        initResults();

        log.info("Executing query: {}", query); // 추가된 로그

        fetchQuery(query);
    }

    protected void clearIfTransacted() {
        if (transacted) {
            entityManager.clear();
        }
    }

    protected JPAQuery<T> createQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFunction.apply(queryFactory);
    }

    protected void initResults() {
        if (CollectionUtils.isEmpty(results)) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
        log.info("여기는 타나요?initResults() 00000000000000000000000000000000000000000000");
    }


    // QuerydslPagingItemReader 클래스의 fetchQuery 메서드
    protected void fetchQuery(JPAQuery<T> query) {
        if (!transacted) {
            List<T> queryResult = query.fetch();
            for (T entity : queryResult) {
                entityManager.detach(entity);
                results.add(entity);
                log.info("Detached and added entity: {}", entity); // 추가된 로그
            }
        } else {
            List<T> queryResult = query.fetch();
            results.addAll(queryResult);
            log.info("Added entities to results: {}", queryResult); // 추가된 로그
        }
    }

    @Override
    protected void doJumpToPage(int itemIndex) {

    }

    @Override
    protected void doClose() throws Exception {
        entityManager.close();
        super.doClose();
    }
}