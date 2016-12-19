package ph.codeia.pipad;

public interface Config {

    String HOST = "host";
    String PORT = "port";
    String RIGHT_HANDED = "right-handed";
    String SPEED = "pointer-speed";

    Config DEFAULT = new Config() {
        @Override
        public String host() {
            return "192.168.0.28";
        }

        @Override
        public int port() {
            return 6000;
        }

        @Override
        public boolean rightHanded() {
            return true;
        }

        @Override
        public float speed() {
            return 1.25f;
        }
    };

    String host();
    int port();
    boolean rightHanded();
    float speed();

}
