const PermissionInfo = {
    permissions: [],
    init: function (array) {
        for (let name of array) {
            this.permissions.push(name);
        }
    },
    hasPermission: function (name) {
        return this.permissions.includes(name);
    }
}
export default PermissionInfo;