package experian.mobilesdk.network;

import okhttp3.Cache;

/** Basic abstraction for REST controllers that consume API with support for Caches */
public interface RestController {

  /** Method for creating a cached file used to stored temporary data for requests */
  Cache createCache();

  /**
   * Method for removing/evicting all the Cache, ideally called during Logout Make sure that a call
   * to {@code initialize()} was properly made first before calling this method.
   */
  void evictAllCache();

  /**
   * Removes cached responses which contains the specified path. Make sure that a call to {@code
   * initialize()} was properly made first before calling this method.
   *
   * @param path the path that will be used to search the cached data.
   */
  void removeResponsesFromCache(String path);
}
