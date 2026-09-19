package com.finance.voiceai.repository;

import com.finance.voiceai.domain.entity.Category;
import com.finance.voiceai.domain.entity.Transaction;
import com.finance.voiceai.domain.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop20ByOrderByDateDesc();

    List<Transaction> findByCategoryOrderByDateDesc(Category category);

    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category = :category AND t.type = :type")
    BigDecimal sumAmountByCategoryAndType(@Param("category") Category category, @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.category = :category AND t.type = :type AND t.date BETWEEN :start AND :end")
    BigDecimal sumAmountByCategoryAndTypeInPeriod(@Param("category") Category category,
                                                 @Param("type") TransactionType type,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}
