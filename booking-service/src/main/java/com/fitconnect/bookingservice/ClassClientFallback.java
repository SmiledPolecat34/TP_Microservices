package com.fitconnect.bookingservice;
import org.springframework.cloud.openfeign.FallbackFactory; import org.springframework.stereotype.Component;
@Component public class ClassClientFallback implements FallbackFactory<ClassClient> {public ClassClient create(Throwable cause){RuntimeException e=DownstreamUnavailableException.from("class-service",cause);return new ClassClient(){public Contracts.ClassView get(Long id){throw e;}public Contracts.ClassView increment(Long id,int spots){throw e;}public Contracts.ClassView decrement(Long id,int spots){throw e;}};}}
