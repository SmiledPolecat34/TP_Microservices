package com.fitconnect.classservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false"})
@AutoConfigureMockMvc
class FitnessClassIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired FitnessClassRepository repository;
  @Autowired FitnessClassController controller;

  private FitnessClass newClass(int max,int current){
    return repository.save(FitnessClass.builder().name("Yoga Flow").description("Test").instructor("Marie").gymLocation("Paris").category(FitnessClass.Category.YOGA).level(FitnessClass.Level.BEGINNER).durationMinutes(60).maxParticipants(max).currentParticipants(current).price(new BigDecimal("20.00")).dateTime(LocalDateTime.now().plusDays(3)).build());
  }

  @Test void shouldCreateClassThroughApi() throws Exception {
    String body="{\"name\":\"Pilates\",\"description\":\"Test\",\"instructor\":\"Marie\",\"gymLocation\":\"Paris\",\"category\":\"PILATES\",\"level\":\"BEGINNER\",\"durationMinutes\":45,\"maxParticipants\":10,\"price\":12.50,\"dateTime\":\"2030-10-10T18:00:00\"}";
    mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SCHEDULED")).andExpect(jsonPath("$.currentParticipants").value(0));
    mvc.perform(post("/api/classes").contentType(MediaType.APPLICATION_JSON).content(body.replace("45","50"))).andExpect(status().isBadRequest());
  }

  @Test void shouldIncrementAndDecrementSpots() throws Exception {
    Long id=newClass(10,5).getId();
    mvc.perform(patch("/api/classes/{id}/increment",id).param("spots","2")).andExpect(status().isOk()).andExpect(jsonPath("$.currentParticipants").value(7));
    mvc.perform(patch("/api/classes/{id}/decrement",id).param("spots","2")).andExpect(status().isOk()).andExpect(jsonPath("$.currentParticipants").value(5));
    mvc.perform(get("/api/classes/{id}",id)).andExpect(jsonPath("$.currentParticipants").value(5));
  }

  @Test void shouldReturn409_whenNoSpotsAvailable() throws Exception {
    Long id=newClass(10,9).getId();
    mvc.perform(patch("/api/classes/{id}/increment",id).param("spots","2")).andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("Plus de places disponibles pour ce cours"));
    assertEquals(9,repository.findById(id).orElseThrow().getCurrentParticipants());
  }

  @Test void shouldNeverOverbook_whenBookingsAreConcurrent() throws Exception {
    Long id=newClass(5,0).getId();
    int threads=8; ExecutorService pool=Executors.newFixedThreadPool(threads); CountDownLatch start=new CountDownLatch(1); AtomicInteger accepted=new AtomicInteger();
    for(int i=0;i<threads;i++) pool.submit(()->{ try{ start.await(); controller.increment(id,1); accepted.incrementAndGet(); }catch(Exception refused){ /* complet ou conflit de version */ } });
    start.countDown(); pool.shutdown(); assertTrue(pool.awaitTermination(30,TimeUnit.SECONDS));
    int participants=repository.findById(id).orElseThrow().getCurrentParticipants();
    assertEquals(accepted.get(),participants);
    assertTrue(participants<=5,"surréservation: "+participants);
  }
}
