#!/bin/sh

# --batch to prevent interactive command --yes to assume "yes" for questions
gpg --version
gpg --verbose --batch --yes --decrypt --passphrase="$APP_PROPS_SECRET" --output $HOME/app.props app.props.gpg
