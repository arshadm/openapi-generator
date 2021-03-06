/**
* OpenAPI Petstore
* This is a sample server Petstore server. For this sample, you can use the api key `special-key` to test the authorization filters.
*
* OpenAPI spec version: 1.0.0
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/

#include "pistache/endpoint.h"
#include "pistache/http.h"
#include "pistache/router.h"
#include "StoreApiImpl.h"

using namespace org::openapitools::server::api;

int main() {
    Pistache::Address addr(Pistache::Ipv4::any(), Pistache::Port(8080));

    StoreApiImpl server(addr);
    server.init(2);
    server.start();

    server.shutdown();
}

