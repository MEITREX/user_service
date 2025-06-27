# User Service API

<details>
  <summary><strong>Table of Contents</strong></summary>

  * [Query](#query)
  * [Mutation](#mutation)
  * [Objects](#objects)
    * [AccessToken](#accesstoken)
    * [ExternalUserIdWithUser](#externaluseridwithuser)
    * [Notification](#notification)
    * [PaginationInfo](#paginationinfo)
    * [PublicUserInfo](#publicuserinfo)
    * [Settings](#settings)
    * [UserInfo](#userinfo)
  * [Inputs](#inputs)
    * [DateTimeFilter](#datetimefilter)
    * [GenerateAccessTokenInput](#generateaccesstokeninput)
    * [IntFilter](#intfilter)
    * [NotificationInput](#notificationinput)
    * [Pagination](#pagination)
    * [SettingsInput](#settingsinput)
    * [StringFilter](#stringfilter)
  * [Enums](#enums)
    * [ExternalServiceProviderDto](#externalserviceproviderdto)
    * [Gamification](#gamification)
    * [GlobalUserRole](#globaluserrole)
    * [SortDirection](#sortdirection)
  * [Scalars](#scalars)
    * [Boolean](#boolean)
    * [Date](#date)
    * [DateTime](#datetime)
    * [Int](#int)
    * [LocalTime](#localtime)
    * [String](#string)
    * [Time](#time)
    * [UUID](#uuid)
    * [Url](#url)

</details>

## Query
<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="query.findpublicuserinfos">findPublicUserInfos</strong></td>
<td valign="top">[<a href="#publicuserinfo">PublicUserInfo</a>]!</td>
<td>

Gets the publicly available information for a list of users with the specified IDs.
If a user does not exist, null is returned for that user.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">ids</td>
<td valign="top">[<a href="#uuid">UUID</a>!]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query.currentuserinfo">currentUserInfo</strong></td>
<td valign="top"><a href="#userinfo">UserInfo</a>!</td>
<td>

Gets the user information of the currently authorized user.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query.finduserinfos">findUserInfos</strong></td>
<td valign="top">[<a href="#userinfo">UserInfo</a>]!</td>
<td>

Gets all of the users' information for a list of users with the specified IDs.
Only available to privileged users.
If a user does not exist, null is returned for that user.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">ids</td>
<td valign="top">[<a href="#uuid">UUID</a>!]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query.isaccesstokenavailable">isAccessTokenAvailable</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>

Checks whether an access token for a given third-party provider exists and is still valid for the currently authenticated user.
Returns `true` if:
- The access token exists and is not expired, OR
- The refresh token exists and is not expired.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">provider</td>
<td valign="top"><a href="#externalserviceproviderdto">ExternalServiceProviderDto</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query._internal_noauth_getaccesstoken">_internal_noauth_getAccessToken</strong></td>
<td valign="top"><a href="#accesstoken">AccessToken</a>!</td>
<td>

Retrieves the access token of the specified user for a specified third-party provider.

- If the access token is expired but a valid refresh token is available, the system will attempt to generate a new access token.
- Throws an exception if neither a valid access token nor a valid refresh token is available.

⚠️ This query is **only accessible internally**. ⚠️

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">userId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">provider</td>
<td valign="top"><a href="#externalserviceproviderdto">ExternalServiceProviderDto</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query._internal_noauth_getexternaluserids">_internal_noauth_getExternalUserIds</strong></td>
<td valign="top">[<a href="#externaluseridwithuser">ExternalUserIdWithUser</a>!]!</td>
<td>

Retrieves the external user IDs for a list of users with the specified IDs.
If a user does not exist, null is returned for that user.
If the user does not have an external user ID for the specified provider, null is returned for that user.
⚠️ This query is **only accessible internally** and the caller must be at least a tutor of a course. The permission must be validated by the caller method. ⚠️

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">userIds</td>
<td valign="top">[<a href="#uuid">UUID</a>!]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">provider</td>
<td valign="top"><a href="#externalserviceproviderdto">ExternalServiceProviderDto</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query.findusersettings">findUserSettings</strong></td>
<td valign="top"><a href="#settings">Settings</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">userId</td>
<td valign="top"><a href="#uuid">UUID</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="query.finduserssettings">findUsersSettings</strong></td>
<td valign="top">[<a href="#settings">Settings</a>]!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">usersIds</td>
<td valign="top">[<a href="#uuid">UUID</a>]!</td>
<td></td>
</tr>
</tbody>
</table>

## Mutation
<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="mutation.generateaccesstoken">generateAccessToken</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>

Generates an access token for the given provider using an authorization code obtained from the OAuth flow.
This should be called **only after** the user completes authorization and the frontend retrieves the auth code.
After the access token is generated, the user is redirected to the redirect URI.

</td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#generateaccesstokeninput">GenerateAccessTokenInput</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="mutation.updatesettings">updateSettings</strong></td>
<td valign="top"><a href="#settings">Settings</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">userId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">input</td>
<td valign="top"><a href="#settingsinput">SettingsInput</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="mutation.defaultsettings">defaultSettings</strong></td>
<td valign="top"><a href="#settings">Settings</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" align="right" valign="top">userId</td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
</tbody>
</table>

## Objects

### AccessToken

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="accesstoken.accesstoken">accessToken</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="accesstoken.externaluserid">externalUserId</strong></td>
<td valign="top"><a href="#string">String</a></td>
<td></td>
</tr>
</tbody>
</table>

### ExternalUserIdWithUser

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="externaluseridwithuser.userid">userId</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="externaluseridwithuser.externaluserid">externalUserId</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
</tbody>
</table>

### Notification

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="notification.gamification">gamification</strong></td>
<td valign="top"><a href="#boolean">Boolean</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="notification.lecture">lecture</strong></td>
<td valign="top"><a href="#boolean">Boolean</a></td>
<td></td>
</tr>
</tbody>
</table>

### PaginationInfo

Return type for information about paginated results.

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="paginationinfo.page">page</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The current page number.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="paginationinfo.size">size</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The number of elements per page.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="paginationinfo.totalelements">totalElements</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The total number of elements across all pages.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="paginationinfo.totalpages">totalPages</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The total number of pages.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="paginationinfo.hasnext">hasNext</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>

Whether there is a next page.

</td>
</tr>
</tbody>
</table>

### PublicUserInfo

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="publicuserinfo.id">id</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="publicuserinfo.username">userName</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
</tbody>
</table>

### Settings

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="settings.gamification">gamification</strong></td>
<td valign="top"><a href="#gamification">Gamification</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="settings.notification">notification</strong></td>
<td valign="top"><a href="#notification">Notification</a></td>
<td></td>
</tr>
</tbody>
</table>

### UserInfo

<table>
<thead>
<tr>
<th align="left">Field</th>
<th align="right">Argument</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="userinfo.id">id</strong></td>
<td valign="top"><a href="#uuid">UUID</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="userinfo.username">userName</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="userinfo.firstname">firstName</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="userinfo.lastname">lastName</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="userinfo.realmroles">realmRoles</strong></td>
<td valign="top">[<a href="#globaluserrole">GlobalUserRole</a>!]!</td>
<td></td>
</tr>
</tbody>
</table>

## Inputs

### DateTimeFilter

Filter for date values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="datetimefilter.after">after</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>

If specified, filters for dates after the specified value.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="datetimefilter.before">before</strong></td>
<td valign="top"><a href="#datetime">DateTime</a></td>
<td>

If specified, filters for dates before the specified value.

</td>
</tr>
</tbody>
</table>

### GenerateAccessTokenInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="generateaccesstokeninput.provider">provider</strong></td>
<td valign="top"><a href="#externalserviceproviderdto">ExternalServiceProviderDto</a>!</td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="generateaccesstokeninput.authorizationcode">authorizationCode</strong></td>
<td valign="top"><a href="#string">String</a>!</td>
<td></td>
</tr>
</tbody>
</table>

### IntFilter

Filter for integer values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="intfilter.equals">equals</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>

An integer value to match exactly.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="intfilter.greaterthan">greaterThan</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>

If specified, filters for values greater than to the specified value.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="intfilter.lessthan">lessThan</strong></td>
<td valign="top"><a href="#int">Int</a></td>
<td>

If specified, filters for values less than to the specified value.

</td>
</tr>
</tbody>
</table>

### NotificationInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="notificationinput.gamification">gamification</strong></td>
<td valign="top"><a href="#boolean">Boolean</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="notificationinput.lecture">lecture</strong></td>
<td valign="top"><a href="#boolean">Boolean</a></td>
<td></td>
</tr>
</tbody>
</table>

### Pagination

Specifies the page size and page number for paginated results.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="pagination.page">page</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The page number, starting at 0.
If not specified, the default value is 0.
For values greater than 0, the page size must be specified.
If this value is larger than the number of pages, an empty page is returned.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="pagination.size">size</strong></td>
<td valign="top"><a href="#int">Int</a>!</td>
<td>

The number of elements per page.

</td>
</tr>
</tbody>
</table>

### SettingsInput

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="settingsinput.gamification">gamification</strong></td>
<td valign="top"><a href="#gamification">Gamification</a></td>
<td></td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="settingsinput.notification">notification</strong></td>
<td valign="top"><a href="#notificationinput">NotificationInput</a></td>
<td></td>
</tr>
</tbody>
</table>

### StringFilter

Filter for string values.
If multiple filters are specified, they are combined with AND.

<table>
<thead>
<tr>
<th colspan="2" align="left">Field</th>
<th align="left">Type</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td colspan="2" valign="top"><strong id="stringfilter.equals">equals</strong></td>
<td valign="top"><a href="#string">String</a></td>
<td>

A string value to match exactly.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="stringfilter.contains">contains</strong></td>
<td valign="top"><a href="#string">String</a></td>
<td>

A string value that must be contained in the field that is being filtered.

</td>
</tr>
<tr>
<td colspan="2" valign="top"><strong id="stringfilter.ignorecase">ignoreCase</strong></td>
<td valign="top"><a href="#boolean">Boolean</a>!</td>
<td>

If true, the filter is case-insensitive.

</td>
</tr>
</tbody>
</table>

## Enums

### ExternalServiceProviderDto

<table>
<thead>
<tr>
<th align="left">Value</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td valign="top"><strong>GITHUB</strong></td>
<td></td>
</tr>
</tbody>
</table>

### Gamification

<table>
<thead>
<tr>
<th align="left">Value</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td valign="top"><strong>GAMIFICATION_ENABLED</strong></td>
<td></td>
</tr>
<tr>
<td valign="top"><strong>ADAPTIVE_GAMIFICATION_ENABLED</strong></td>
<td></td>
</tr>
<tr>
<td valign="top"><strong>ALL_GAMIFICATION_DISABLED</strong></td>
<td></td>
</tr>
</tbody>
</table>

### GlobalUserRole

<table>
<thead>
<tr>
<th align="left">Value</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td valign="top"><strong>SUPER_USER</strong></td>
<td></td>
</tr>
<tr>
<td valign="top"><strong>COURSE_CREATOR</strong></td>
<td></td>
</tr>
</tbody>
</table>

### SortDirection

Specifies the sort direction, either ascending or descending.

<table>
<thead>
<tr>
<th align="left">Value</th>
<th align="left">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td valign="top"><strong>ASC</strong></td>
<td></td>
</tr>
<tr>
<td valign="top"><strong>DESC</strong></td>
<td></td>
</tr>
</tbody>
</table>

## Scalars

### Boolean

The `Boolean` scalar type represents `true` or `false`.

### Date

### DateTime

### Int

The `Int` scalar type represents non-fractional signed whole numeric values. Int can represent values between -(2^31) and 2^31 - 1.

### LocalTime

### String

The `String` scalar type represents textual data, represented as UTF-8 character sequences. The String type is most often used by GraphQL to represent free-form human-readable text.

### Time

### UUID

### Url

