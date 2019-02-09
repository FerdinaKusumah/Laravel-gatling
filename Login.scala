/*
 * Copyright 2011-2018 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package starts

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class Login extends Simulation {

    val base_url = "https://example.com"

    val httpProtocol = http
        .baseUrl(base_url) // Here is the root for all relative URLs
        .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8") // Here are the common headers
        .acceptEncodingHeader("gzip, deflate, br")
        .acceptLanguageHeader("id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7")
        .doNotTrackHeader("1")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")

    val header_nonauthorize = Map(
        "Content-Type" -> "application/x-www-form-urlencoded",
        "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0",
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language" -> "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
        "Connection" -> "keep-alive"
    ) // Note the headers specific to a given request

    val header_authorized = Map(
            "Accept" -> """application/json""",
            "XSRF-TOKEN" -> "${csrf_token}"
        )
    
    val header_ajax = Map(
        "Accept" -> """application/json""",
        "XSRF-TOKEN" -> "${csrf_token}",
        "X-Requested-With" -> "XMLHttpRequest"
    )

    val scn = scenario("Scenario Login") // A scenario is a chain of requests and pauses
        .exec(
            http("Get Login")
            .get("/login")
            .check(css("input[type='hidden 'name='_token']", "value").saveAs("csrf_token"))
            .check(status.is(200))
        )
        .pause(15) // Note that Gatling has recorded real time pauses
        .exec(
                http("Post Login") // Here's an example of a POST request
                .post("/login")
                .headers(header_authorized)
                .formParam("_token", "${csrf_token}") // Note the triple double quotes: used in Scala for protecting a whole chain of characters (no need for backslash)
                .formParam("email", "admin@example.com") // Note the triple double quotes: used in Scala for protecting a whole chain of characters (no need for backslash)
                .formParam("password", "password")
                .check(status.is(200))
            )
        // .exec { session => println(session("csrf_token").as[String]); session}


    setUp(
        scn.inject(
            nothingFor(4 seconds), // 1
            atOnceUsers(10), // 2
            rampUsers(10) during (5 seconds), // 3
            constantUsersPerSec(20) during (15 seconds), // 4
            constantUsersPerSec(20) during (15 seconds) randomized, // 5
            rampUsersPerSec(10) to 20 during (2 minutes), // 6
            rampUsersPerSec(10) to 20 during (2 minutes) randomized, // 7
            heavisideUsers(1000) during (20 seconds) // 8
        ).protocols(httpProtocol)
    )

    // nothingFor(duration): Pause for a given duration.
    // atOnceUsers(nbUsers): Injects a given number of users at once.
    // rampUsers(nbUsers) during(duration): Injects a given number of users with a linear ramp over a given duration.
    // constantUsersPerSec(rate) during(duration): Injects users at a constant rate, defined in users per second, during a given duration. Users will be injected at regular intervals.
    // constantUsersPerSec(rate) during(duration) randomized: Injects users at a constant rate, defined in users per second, during a given duration. Users will be injected at randomized intervals.
    // rampUsersPerSec(rate1) to (rate2) during(duration): Injects users from starting rate to target rate, defined in users per second, during a given duration. Users will be injected at regular intervals.
    // rampUsersPerSec(rate1) to(rate2) during(duration) randomized: Injects users from starting rate to target rate, defined in users per second, during a given duration. Users will be injected at randomized intervals.
    // heavisideUsers(nbUsers) during(duration): Injects a given number of users following a smooth approximation of the heaviside step function stretched to a given duration.
    // reachRps(target) in (duration): target a throughput with a ramp over a given duration.
    // jumpToRps(target): jump immediately to a given targeted throughput.
    // holdFor(duration): hold the current throughput for a given duration.
}
