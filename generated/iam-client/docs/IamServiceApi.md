# IamServiceApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**iamServiceHandshake**](IamServiceApi.md#iamServiceHandshake) | **POST** v1/handshake |  |
| [**iamServiceLogin**](IamServiceApi.md#iamServiceLogin) | **POST** v1/login |  |
| [**iamServiceStoreOwnerMutate**](IamServiceApi.md#iamServiceStoreOwnerMutate) | **POST** v1/store-owners:mutate |  |
| [**iamServiceStoreOwnerSearch**](IamServiceApi.md#iamServiceStoreOwnerSearch) | **POST** v1/store-owners:search |  |
| [**iamServiceStoreOwnerSearchByTiles**](IamServiceApi.md#iamServiceStoreOwnerSearchByTiles) | **POST** v1/store-owners:search-by-tiles |  |





### Example
```kotlin
// Import classes:
//import com.aliasadi.iam.client.*
//import com.aliasadi.iam.client.infrastructure.*
//import com.aliasadi.iam.client.dto.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(IamServiceApi::class.java)
val body : V1HandshakeRequest =  // V1HandshakeRequest | 

launch(Dispatchers.IO) {
    val result : V1BaseResponse = webService.iamServiceHandshake(body)
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **body** | [**V1HandshakeRequest**](V1HandshakeRequest.md)|  | |

### Return type

[**V1BaseResponse**](V1BaseResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json




### Example
```kotlin
// Import classes:
//import com.aliasadi.iam.client.*
//import com.aliasadi.iam.client.infrastructure.*
//import com.aliasadi.iam.client.dto.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(IamServiceApi::class.java)
val body : V1LoginRequest =  // V1LoginRequest | 

launch(Dispatchers.IO) {
    val result : V1BaseResponse = webService.iamServiceLogin(body)
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **body** | [**V1LoginRequest**](V1LoginRequest.md)|  | |

### Return type

[**V1BaseResponse**](V1BaseResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json




### Example
```kotlin
// Import classes:
//import com.aliasadi.iam.client.*
//import com.aliasadi.iam.client.infrastructure.*
//import com.aliasadi.iam.client.dto.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(IamServiceApi::class.java)
val body : V1StoreOwnerMutateRequest =  // V1StoreOwnerMutateRequest | 

launch(Dispatchers.IO) {
    val result : V1MutateResponse = webService.iamServiceStoreOwnerMutate(body)
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **body** | [**V1StoreOwnerMutateRequest**](V1StoreOwnerMutateRequest.md)|  | |

### Return type

[**V1MutateResponse**](V1MutateResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json




### Example
```kotlin
// Import classes:
//import com.aliasadi.iam.client.*
//import com.aliasadi.iam.client.infrastructure.*
//import com.aliasadi.iam.client.dto.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(IamServiceApi::class.java)
val body : V1StoreOwnerSearchRequest =  // V1StoreOwnerSearchRequest | 

launch(Dispatchers.IO) {
    val result : V1BaseResponse = webService.iamServiceStoreOwnerSearch(body)
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **body** | [**V1StoreOwnerSearchRequest**](V1StoreOwnerSearchRequest.md)|  | |

### Return type

[**V1BaseResponse**](V1BaseResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json




### Example
```kotlin
// Import classes:
//import com.aliasadi.iam.client.*
//import com.aliasadi.iam.client.infrastructure.*
//import com.aliasadi.iam.client.dto.*

val apiClient = ApiClient()
val webService = apiClient.createWebservice(IamServiceApi::class.java)
val body : V1StoreOwnerSearchByTilesRequest =  // V1StoreOwnerSearchByTilesRequest | 

launch(Dispatchers.IO) {
    val result : V1BaseResponse = webService.iamServiceStoreOwnerSearchByTiles(body)
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **body** | [**V1StoreOwnerSearchByTilesRequest**](V1StoreOwnerSearchByTilesRequest.md)|  | |

### Return type

[**V1BaseResponse**](V1BaseResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

