import WebSocketConnector from "@/api/websocket.js";
import {useCookies} from "vue3-cookies";
import router from "@/router/index.js";

const {cookies} = useCookies();

WebSocketConnector.registerAction("addUser", (message) => {
    const user = message.data;
    UserDataInterface.users[user.qq] = user;
    if (UserDataInterface.userGroupMapping[user.role]) {
        UserDataInterface.userGroupMapping[user.role][user.qq] = user;
    }
});
WebSocketConnector.registerAction("updateUser", (message) => {
    const updatedUserData = message.data;
    for (const key in UserDataInterface.users[updatedUserData.qq]) {
        UserDataInterface.users[updatedUserData.qq][key] = updatedUserData[key];
    }
    if (updatedUserData.qq === UserDataInterface.currentUser.value.qq) {
        UserDataInterface.currentUser.value.name = updatedUserData.name;
        UserDataInterface.currentUser.value.role = updatedUserData.role;
    }
    for (const [roleName, usersOfRole] of Object.entries(UserDataInterface.userGroupMapping)) {
        if (Boolean(usersOfRole)) {
            const previousUserData = usersOfRole[updatedUserData.qq];
            if (previousUserData) {
                if (previousUserData.role !== updatedUserData.role) {
                    delete usersOfRole[updatedUserData.qq];
                }
            }
            if (roleName === updatedUserData.role) {
                usersOfRole[updatedUserData.qq] = updatedUserData;
            }
        }
    }
});
WebSocketConnector.registerAction("deleteUser", (message) => {
    delete UserDataInterface.users[message.data.qq];
    if (UserDataInterface.userGroupMapping[message.data.role])
        delete UserDataInterface.userGroupMapping[message.data.role][message.data.qq];
});
WebSocketConnector.registerAction("addRole", (message) => {
    UserDataInterface.userGroups[message.data.role.type] = message.data.role;
});
WebSocketConnector.registerAction("deleteRole", (message) => {
    delete UserDataInterface.userGroups[message.data.role.type];
});
WebSocketConnector.registerAction("addPermission", (message) => {
    for (const userGroupType in UserDataInterface.userGroups) {
        const userGroup = UserDataInterface.userGroups[userGroupType];
        if (userGroup.permissionGroups) {
            for (const permissionGroup of userGroup.permissionGroups) {
                if (permissionGroup.name === message,data.permission.group) {
                    permissionGroup.permissions.push(message.data.permission);
                    break;
                }
            }
        }
    }
});
WebSocketConnector.registerAction("deletePermission", (message) => {
    for (const userGroupType in UserDataInterface.userGroups) {
        const userGroup = UserDataInterface.userGroups[userGroupType];
        if (userGroup.permissionGroups) {
            for (const permissionGroup of userGroup.permissionGroups) {
                const permission1 = message.data.permission;
                if (permissionGroup.name === permission1.group) {
                    for (const permission of permissionGroup.permissions) {
                        if (permission.name === permission1.name) {
                            permissionGroup.permissions.splice(permissionGroup.permissions.indexOf(permission), 1);
                            break;
                        }
                    }
                }
            }
        }
    }
});
WebSocketConnector.registerAction("updatePermissions", (message) => {
    const currentRole = UserDataInterface.currentUser.value.role;
    const userGroup = UserDataInterface.userGroups[currentRole];
    if (userGroup && userGroup.permissionGroups) {
        for (const permissionGroup of userGroup.permissionGroups) {
            const updatedPermissionsOfGroup = message.data.permissions[permissionGroup.name];
            if (updatedPermissionsOfGroup) {
                for (const permission of permissionGroup.permissions) {
                    permission.enabled = Boolean(updatedPermissionsOfGroup.find(permission1 => permission1.name === permission.name));
                }
            }
        }
    }
});

/*
WebSocketConnector.registerAction("pushRole", (message) => {
    UserDataInterface.currentUser.value.role = message.data.role;
});
*/

const UserDataInterface = {
    users: reactive({}),
    userGroups: reactive({}),
    userGroupMapping: reactive({}),
    currentUser: ref({qq: -1, name: "unknown", role: "unknown"}),
    logined: ref(false),
    getUsersAsync: function () {
        return new Promise((resolve, reject) => {
            if (Object.keys(UserDataInterface.users).length !== 0) {
                resolve(UserDataInterface.users);
            } else {
                WebSocketConnector.send({
                    type: "getAllUsers",
                }).then((response) => {
                    for (const user of response.data.users) {
                        UserDataInterface.users[user.qq] = user;
                    }
                    resolve(UserDataInterface.users);
                }, (error) => {
                    reject(error);
                });
            }
        });
    },
    getUserOfQQAsync: function (qq) {
        return new Promise((resolve, reject) => {
            if (Object.keys(UserDataInterface.users).length === 0) {
                UserDataInterface.getUsersAsync().then(() => {
                    resolve(reactive(UserDataInterface.getUserOfQQAsync(qq)));
                });
            } else {
                let user = reactive(UserDataInterface.users[qq]);
                if (user === undefined) {
                    resolve({qq: -1, name: "unknown", role: "unknown"});
                }
                resolve(user);
            }
        });
    },
    getCurrentUser: () => {
        return UserDataInterface.currentUser;
    },
    logout: () => {
        cookies.remove("user", "/checkIn");
        cookies.remove("token", "/checkIn");
        WebSocketConnector.close();
        UserDataInterface.logined.value = false;
        router.push({name: 'login'});
    },
    getReactiveUserGroupsAsync: () => {
        return new Promise((resolve, reject) => {
            if (Object.keys(UserDataInterface.userGroups) > 0) {
                resolve(UserDataInterface.userGroups);
            } else {
                WebSocketConnector.send({
                    type: "getAllRoles",
                }).then((response) => {
                    for (const role of response.data.roles) {
                        UserDataInterface.userGroups[role.type] = role;
                    }
                    resolve(UserDataInterface.userGroups);
                }, (error) => {
                    reject(error);
                });
            }
        });
    },
    getUserGroupAsync: (type) => {
        return new Promise((resolve, reject) => {
            UserDataInterface.getReactiveUserGroupsAsync().then((userGroups) => {
                const userGroup = userGroups[type];
                if (userGroup)
                    resolve(userGroup);
                else
                    reject("No such user group.");
            });
        });
    },
    getUsersOfUserGroupSync: (groupType) => {
        return new Promise((resolve, reject) => {
            if (UserDataInterface.userGroupMapping[groupType] === undefined) {
                WebSocketConnector.send({
                    type: "getUsersOfRole",
                    data: {
                        roleType: groupType
                    }
                }).then((response) => {
                    if (!UserDataInterface.userGroupMapping[groupType]) {
                        UserDataInterface.userGroupMapping[groupType] = {};
                    }
                    for (const user of response.data.users) {
                        UserDataInterface.userGroupMapping[groupType][user.qq] = user;
                    }
                    resolve(UserDataInterface.userGroupMapping[groupType]);
                }, (err) => {
                    reject(err);
                });
            } else {
                resolve(UserDataInterface.userGroupMapping[groupType]);
            }
        });
    },
    getPermissionGroupsOfGroupAsync: (groupType) => {
        return new Promise((resolve, reject) => {
            WebSocketConnector.send({
                type: "getPermissionsOfRole",
                data: {
                    roleType: groupType
                }
            }).then((response) => {
                UserDataInterface.userGroups[groupType].permissionGroups = response.data.permissionGroups;
                resolve(UserDataInterface.userGroups[groupType].permissionGroups);
            }, (err) => {
                reject(err);
            });
        })
    },
    savePermissionsGroupOfUserGroup: (type, permissionGroups) => {
        let enables = [];
        for (const permissionGroup of permissionGroups) {
            for (const permission of permissionGroup.permissions) {
                if (permission.enabled) {
                    enables.push(permission.name);
                }
            }
        }
        console.log(enables);
        return WebSocketConnector.send({
            type: "savePermissionsOfRole",
            data: {
                roleType: type,
                enables: enables
            }
        });
    },
    loginAs: (user) => {
        return new Promise((resolve, reject) => {
            try {
                UserDataInterface.currentUser.value = user;
                watch(() => UserDataInterface.currentUser.value, () => {
                    cookies.set("user", UserDataInterface.currentUser.value, "8h", "/checkIn");
                    cookies.set("token", UserDataInterface.currentUser.value.token, "8h", "/checkIn");
                }, {deep: true, immediate: true});
                const connect = (user) => {
                    try {
                        WebSocketConnector.connect(user.qq, user.token).then(() => {
                            UserDataInterface.logined.value = true;
                            resolve();
                        }, (err) => {
                            reject(err);
                        });
                    } catch (err) {
                        reject(err);
                    }
                }
                connect(user);
                if (window.location.pathname === "/checkIn/login/") {
                    router.push({name: "home"});
                }
            } catch (e) {
                reject(e);
            }
        });
    },
    onLoginFailed: (err) => {
    },
    onLoginSucceed: () => {
    },
}
export default UserDataInterface