package io.appwish.graphqlapi.graphql.fetcher;

import graphql.schema.DataFetchingEnvironment;
import io.appwish.graphqlapi.eventbus.Address;
import io.appwish.grpc.AllWishQueryProto;
import io.appwish.grpc.AllWishReplyProto;
import io.appwish.grpc.UpdateWishInputProto;
import io.appwish.grpc.WishDeleteReplyProto;
import io.appwish.grpc.WishInputProto;
import io.appwish.grpc.WishProto;
import io.appwish.grpc.WishQueryProto;
import io.appwish.grpc.WishReplyProto;
import io.vertx.core.eventbus.EventBus;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Sends events on the event bus to resolve needs of wish-related part of GraphQL schema.
 */
public class WishFetcher {

  private static final String ID = "id";
  private static final String INPUT = "input";
  private static final String TITLE = "title";
  private static final String CONTENT = "content";
  private static final String COVER_IMAGE_URL = "coverImageUrl";

  private final EventBus eventBus;

  public WishFetcher(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public CompletionStage<List<WishProto>> allWish(final DataFetchingEnvironment dataFetchingEnvironment) {
    final CompletableFuture<List<WishProto>> completableFuture = new CompletableFuture<>();
    final AllWishQueryProto query = AllWishQueryProto.newBuilder().build();

    eventBus.<AllWishReplyProto>request(Address.ALL_WISH.get(), query, event -> {
      if (event.succeeded()) {
        completableFuture.complete(event.result().body().getWishesList());
      } else {
        completableFuture.completeExceptionally(event.cause());
      }
    });

    return completableFuture;
  }

  public CompletionStage<WishProto> wish(final DataFetchingEnvironment dataFetchingEnvironment) {
    final CompletableFuture<WishProto> completableFuture = new CompletableFuture<>();
    final WishQueryProto query = WishQueryProto.newBuilder()
      .setId(Long.valueOf(dataFetchingEnvironment.getArgument(ID))).build();

    eventBus.<WishReplyProto>request(Address.WISH.get(), query, event -> {
      if (event.succeeded() && event.result().body().hasWish()) {
        completableFuture.complete(event.result().body().getWish());
      } else if (event.succeeded()) {
        completableFuture.complete(null);
      } else {
        completableFuture.completeExceptionally(event.cause());
      }
    });

    return completableFuture;
  }

  public CompletionStage<WishProto> createWish(final DataFetchingEnvironment dataFetchingEnvironment) {
    final CompletableFuture<WishProto> completableFuture = new CompletableFuture<>();
    final Map<String, String> input = dataFetchingEnvironment.getArgument(INPUT);
    final String title = input.get(TITLE);
    final String content = input.get(CONTENT);
    final String coverImageUrl = input.get(COVER_IMAGE_URL);
    final WishInputProto wishInput = WishInputProto.newBuilder()
      .setTitle(title)
      .setContent(content)
      .setCoverImageUrl(coverImageUrl)
      .build();

    eventBus.<WishReplyProto>request(Address.CREATE_WISH.get(), wishInput, event -> {
      if (event.succeeded() && event.result().body().hasWish()) {
        completableFuture.complete(event.result().body().getWish());
      } else if (event.succeeded()) {
        completableFuture.completeExceptionally(new AssertionError("It should always create and return a wish"));
      } else {
        completableFuture.completeExceptionally(event.cause());
      }
    });

    return completableFuture;
  }

  public CompletionStage<WishProto> updateWish(
    final DataFetchingEnvironment dataFetchingEnvironment) {
    final CompletableFuture<WishProto> completableFuture = new CompletableFuture<>();
    final Map<String, String> input = dataFetchingEnvironment.getArgument(INPUT);
    final String title = input.get(TITLE);
    final String content = input.get(CONTENT);
    final String coverImageUrl = input.get(COVER_IMAGE_URL);
    final UpdateWishInputProto wishInput = UpdateWishInputProto.newBuilder()
      .setTitle(title)
      .setContent(content)
      .setCoverImageUrl(coverImageUrl)
      .setId(Long.valueOf(dataFetchingEnvironment.getArgument(ID)))
      .build();

    eventBus.<WishReplyProto>request(Address.UPDATE_WISH.get(), wishInput,
      event -> {
        if (event.succeeded() && event.result().body().hasWish()) {
          completableFuture.complete(event.result().body().getWish());
        } else if (event.succeeded()) {
          completableFuture.complete(null);
        } else {
          completableFuture.completeExceptionally(event.cause());
        }
      });

    return completableFuture;
  }

  public CompletionStage<Boolean> deleteWish(final DataFetchingEnvironment dataFetchingEnvironment) { final CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
    final WishQueryProto query = WishQueryProto.newBuilder().setId(Long.valueOf(dataFetchingEnvironment.getArgument(ID))).build();

    eventBus.<WishDeleteReplyProto>request(Address.DELETE_WISH.get(), query,
      event -> {
        if (event.succeeded()) {
          completableFuture.complete(event.result().body().getDeleted());
        } else {
          completableFuture.completeExceptionally(event.cause());
        }
      });

    return completableFuture;
  }
}
