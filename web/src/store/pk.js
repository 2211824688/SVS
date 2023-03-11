

export default {
    state: {
        status: "matching", // matching表示匹配界面, playing表示对战界面
        socket: null,
        opponent_username: "",
        opponent_photo: "",
        gamemap: null,
        user1_id: 0,
        user1_sx: 0,
        user1_sy: 0,
        user2_id: 0,
        user2_sx: 0,
        user2_sy: 0,
        gameObject: null,
        loser: "none", // none all user1 user2
    },
    getters: {
    },
    mutations: {
        updateSocket(state, socket) {
            state.socket = socket;
        },
        updateOpponent(state, opponent) {
            state.opponent_username = opponent.username;
            state.opponent_photo = opponent.photo;
        },
        updateStatus(state, status) {
            state.status = status;
        },
        updateGame(state, game) {
            state.gamemap = game.map;
            state.user1_id = game.user1_id;
            state.user1_sx = game.user1_sx;
            state.user1_sy = game.user1_sy;
            state.user2_id = game.user2_id;
            state.user2_sx = game.user2_sx;
            state.user2_sy = game.user2_sy;
        },
        updateGameObject(state, gameObject) {
            state.gameObject = gameObject;
        },
        updateLoser(state, loser) {
            state.loser = loser;
        }
    },
    actions: {

    },
    modules: {
    }
}